package io.osvaldas.backoffice.domain.loans

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.CLOSED
import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING
import static io.osvaldas.api.loans.Status.REJECTED
import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS
import static java.time.ZoneOffset.UTC
import static java.util.concurrent.TimeUnit.SECONDS
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED

import java.time.ZonedDateTime
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeoutException

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.loans.Status
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.api.util.TimeUtils
import io.osvaldas.backoffice.domain.clients.ClientService
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig
import io.osvaldas.backoffice.repositories.AbstractDatabaseSpec
import io.osvaldas.backoffice.repositories.ClientRepository
import io.osvaldas.backoffice.repositories.LoanRepository
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Rollup
import spock.lang.Subject

@Import([LoanService, ClientService])
@Transactional(propagation = NOT_SUPPORTED)
class LoanServiceIntegrationSpec extends AbstractDatabaseSpec {

    static final int LOAN_LIMIT = 2

    static final int PARALLEL_REQUESTS = 6

    @SpringBean
    TimeUtils timeUtils = Stub()

    @SpringBean
    PropertiesConfig config = Stub {
        interestRate >> 10.0
    }

    @SpringBean
    RiskCheckerClient riskCheckerClient = Mock()

    @Subject
    @Autowired
    LoanService loanService

    @Autowired
    ClientRepository clientRepository

    @Autowired
    LoanRepository loanRepository

    ZonedDateTime today = ZonedDateTime.now(UTC)

    ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_REQUESTS)

    void cleanup() {
        executor.shutdownNow()
        loanRepository.deleteAll()
        clientRepository.deleteAll()
    }

    void 'should count only active loans of the client taken today before the evaluated loan'() {
        given:
            timeUtils.currentDateTime >> today
            Client owner = clientRepository.save(client)
            Client otherClient = clientRepository.save(buildOtherClient())
            [OPEN, NOT_EVALUATED, PENDING, REJECTED, CLOSED].each { saveLoan(owner, it) }
            saveLoan(otherClient, OPEN)
            Loan evaluatedLoan = saveLoan(owner, NOT_EVALUATED)
            saveLoan(owner, OPEN)
        when:
            loanService.validate(evaluatedLoan, VALID_CLIENT_ID)
        then:
            1 * riskCheckerClient.validate(new RiskValidationRequest(evaluatedLoan.id, VALID_CLIENT_ID, 3L))
                >> passedValidation()
    }

    void 'should not count loans taken on earlier days'() {
        given:
            timeUtils.currentDateTime >> today.plusDays(1)
            Client owner = clientRepository.save(client)
            saveLoan(owner, OPEN)
            Loan evaluatedLoan = saveLoan(owner, NOT_EVALUATED)
        when:
            loanService.validate(evaluatedLoan, VALID_CLIENT_ID)
        then:
            1 * riskCheckerClient.validate(new RiskValidationRequest(evaluatedLoan.id, VALID_CLIENT_ID, 0L))
                >> passedValidation()
    }

    void 'should make parallel loan request of the same client wait until earlier one is committed'() {
        given:
            CountDownLatch firstLoanLocked = new CountDownLatch(1)
            CountDownLatch firstLoanReleased = new CountDownLatch(1)
            timeUtils.currentDateTime >> {
                firstLoanLocked.countDown()
                firstLoanReleased.await(10, SECONDS)
                today
            } >> today
            clientRepository.save(client)
        when:
            Future<Loan> firstLoan = executor.submit({ takeLoan() } as Callable<Loan>)
            firstLoanLocked.await(10, SECONDS)
            Future<Loan> secondLoan = executor.submit({ takeLoan() } as Callable<Loan>)
            secondLoan.get(1, SECONDS)
        then:
            thrown(TimeoutException)
        when:
            firstLoanReleased.countDown()
        then:
            firstLoan.get(10, SECONDS).id < secondLoan.get(10, SECONDS).id
    }

    @Rollup
    void 'should open no more loans than daily limit when same client requests them in parallel'() {
        given:
            timeUtils.currentDateTime >> today
            riskCheckerClient.validate(_ as RiskValidationRequest) >> { RiskValidationRequest request ->
                checkLoanLimit(request)
            }
            clientRepository.save(client)
            CountDownLatch start = new CountDownLatch(1)
        when:
            List<Future<Status>> requests = (1..PARALLEL_REQUESTS).collect {
                executor.submit({
                    start.await()
                    takeAndValidateLoan()
                } as Callable<Status>)
            }
            start.countDown()
        then:
            requests*.get(30, SECONDS).count(OPEN) == LOAN_LIMIT
        and:
            loanRepository.findAll()*.status.count(OPEN) == LOAN_LIMIT
        where:
            attempt << (1..10)
    }

    private Loan takeLoan() {
        loanService.addLoan(new Loan().tap {
            amount = 100.00
            termInMonths = 12
        }, VALID_CLIENT_ID)
    }

    private Status takeAndValidateLoan() {
        Loan loan = takeLoan()
        try {
            loanService.validate(loan, VALID_CLIENT_ID)
        } catch (ValidationRuleException ignored) {
            return REJECTED
        }
        loan.status
    }

    private RiskValidationResponse checkLoanLimit(RiskValidationRequest request) {
        request.loansTakenToday() >= LOAN_LIMIT
            ? new RiskValidationResponse(false, LOAN_LIMIT_EXCEEDS)
            : passedValidation()
    }

    private RiskValidationResponse passedValidation() {
        new RiskValidationResponse(true, 'Risk validation passed.')
    }

    private Loan saveLoan(Client loanClient, Status loanStatus) {
        loanRepository.save(new Loan().tap {
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
            client = loanClient
            status = loanStatus
        })
    }

    private Client buildOtherClient() {
        new Client().tap {
            id = INVALID_CLIENT_ID
            firstName = 'Other'
            lastName = 'User'
            email = 'other@mail.com'
            phoneNumber = '+37062541366'
            personalCode = INVALID_PERSONAL_CODE
            status = ACTIVE
        }
    }

}
