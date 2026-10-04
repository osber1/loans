package io.osvaldas.backoffice.domain.loans

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.CLOSED
import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.REJECTED
import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS
import static java.time.ZoneOffset.UTC
import static java.util.concurrent.TimeUnit.SECONDS
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED

import java.sql.Timestamp
import java.time.Clock
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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.loans.Status
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.api.risk.validation.RiskRejectionReason
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.backoffice.domain.clients.ClientService
import io.osvaldas.backoffice.domain.notifications.NotificationOutboxService
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
    Clock clock = Stub()

    @SpringBean
    PropertiesConfig config = Stub {
        interestRate >> 10.0
    }

    @SpringBean
    RiskCheckerClient riskCheckerClient = Mock()

    @SpringBean
    NotificationOutboxService notificationOutbox = Mock()

    @Subject
    @Autowired
    LoanService loanService

    @Autowired
    ClientRepository clientRepository

    @Autowired
    LoanRepository loanRepository

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    TransactionTemplate transactionTemplate

    ZonedDateTime today = ZonedDateTime.now(UTC)

    ZonedDateTime noon = ZonedDateTime.parse('2022-03-15T12:00:00Z')

    RiskValidationRequest sentRequest

    ExecutorService executor = Executors.newFixedThreadPool(PARALLEL_REQUESTS)

    void cleanup() {
        executor.shutdownNow()
        loanRepository.deleteAll()
        clientRepository.deleteAll()
    }

    void 'should count only active loans of the client taken today before the evaluated loan'() {
        given:
            clock.instant() >> (noon.plusHours(1)).toInstant()
            clock.zone >> (noon.plusHours(1)).zone
            Client owner = clientRepository.save(client)
            Client otherClient = clientRepository.save(buildOtherClient())
            [OPEN, NOT_EVALUATED, REJECTED, CLOSED].each { saveLoan(owner, it) }
            saveLoan(owner, OPEN, noon.minusDays(1))
            saveLoan(otherClient, OPEN)
            Loan evaluatedLoan = saveLoan(owner, NOT_EVALUATED)
            saveLoan(owner, OPEN)
        when:
            loanService.validate(evaluatedLoan, VALID_CLIENT_ID)
        then:
            1 * riskCheckerClient.validate(_ as RiskValidationRequest) >> { RiskValidationRequest request ->
                sentRequest = request
                passedValidation()
            }
        and:
            sentRequest.loansTakenToday() == 2L
            sentRequest.requestedAt().toInstant() == noon.toInstant()
    }

    void 'should not count loans taken on earlier days'() {
        given:
            clock.instant() >> (noon).toInstant()
            clock.zone >> (noon).zone
            Client owner = clientRepository.save(client)
            saveLoan(owner, OPEN, noon.minusDays(1))
            Loan evaluatedLoan = saveLoan(owner, NOT_EVALUATED)
        when:
            loanService.validate(evaluatedLoan, VALID_CLIENT_ID)
        then:
            1 * riskCheckerClient.validate(_ as RiskValidationRequest) >> { RiskValidationRequest request ->
                sentRequest = request
                passedValidation()
            }
        and:
            sentRequest.loansTakenToday() == 0L
            sentRequest.requestedAt().toInstant() == noon.toInstant()
    }

    void 'should count the day the loan was requested when it is evaluated after midnight'() {
        given: 'a loan requested at 23:58 that is only evaluated at 00:10 of the next day'
            ZonedDateTime requestedAt = noon.minusDays(1).withHour(23).withMinute(58)
            clock.instant() >> (noon.withHour(0).withMinute(10)).toInstant()
            clock.zone >> (noon.withHour(0).withMinute(10)).zone
            Client owner = clientRepository.save(client)
            saveLoan(owner, OPEN, noon.minusDays(2))
            saveLoan(owner, OPEN, requestedAt.minusHours(12))
            saveLoan(owner, OPEN, requestedAt.minusMinutes(30))
            Loan evaluatedLoan = saveLoan(owner, NOT_EVALUATED, requestedAt)
            saveLoan(owner, OPEN, noon.withHour(0).withMinute(5))
        when:
            loanService.validate(evaluatedLoan, VALID_CLIENT_ID)
        then:
            1 * riskCheckerClient.validate(_ as RiskValidationRequest) >> { RiskValidationRequest request ->
                sentRequest = request
                passedValidation()
            }
        and:
            sentRequest.loansTakenToday() == 2L
            sentRequest.requestedAt().toInstant() == requestedAt.toInstant()
    }

    void 'should use the business time zone to find the day the loan was requested'() {
        given: '22:30 UTC is already after midnight in Vilnius (UTC+3 in summer)'
            ZonedDateTime requestedAt = ZonedDateTime.parse('2022-07-14T22:30:00Z')
            clock.instant() >> (ZonedDateTime.parse('2022-07-15T08:00:00+03:00[Europe/Vilnius]')).toInstant()
            clock.zone >> (ZonedDateTime.parse('2022-07-15T08:00:00+03:00[Europe/Vilnius]')).zone
            Client owner = clientRepository.save(client)
            saveLoan(owner, OPEN, ZonedDateTime.parse('2022-07-14T20:00:00Z'))
            saveLoan(owner, OPEN, ZonedDateTime.parse('2022-07-14T21:30:00Z'))
            Loan evaluatedLoan = saveLoan(owner, NOT_EVALUATED, requestedAt)
        when:
            loanService.validate(evaluatedLoan, VALID_CLIENT_ID)
        then: 'only the loan taken after local midnight (21:00 UTC) counts'
            1 * riskCheckerClient.validate(_ as RiskValidationRequest) >> { RiskValidationRequest request ->
                sentRequest = request
                passedValidation()
            }
        and:
            sentRequest.loansTakenToday() == 1L
            sentRequest.requestedAt().toInstant() == requestedAt.toInstant()
    }

    void 'should not overwrite a loan that was evaluated by someone else in the meantime'() {
        given:
            clock.instant() >> (noon).toInstant()
            clock.zone >> (noon).zone
            riskCheckerClient.validate(_ as RiskValidationRequest) >>> [passedValidation(), passedValidation()]
            Client owner = clientRepository.save(client)
            Long loanId = saveLoan(owner, NOT_EVALUATED).id
            Loan firstCopy = loanRepository.findById(loanId).get()
            Loan secondCopy = loanRepository.findById(loanId).get()
        when:
            loanService.validate(firstCopy, VALID_CLIENT_ID)
            loanService.validate(secondCopy, VALID_CLIENT_ID)
        then:
            thrown(ObjectOptimisticLockingFailureException)
        and:
            loanRepository.findById(loanId).get().status == OPEN
    }

    void 'should make parallel loan request of the same client wait until earlier one is committed'() {
        given:
            CountDownLatch firstLoanLocked = new CountDownLatch(1)
            CountDownLatch firstLoanReleased = new CountDownLatch(1)
            clock.instant() >> {
                firstLoanLocked.countDown()
                firstLoanReleased.await(10, SECONDS)
                today.toInstant()
            } >> today.toInstant()
            clock.zone >> today.zone
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
            clock.instant() >> (today).toInstant()
            clock.zone >> (today).zone
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
            ? RiskValidationResponse.rejected(RiskRejectionReason.LOAN_LIMIT_EXCEEDS, LOAN_LIMIT_EXCEEDS)
            : passedValidation()
    }

    private RiskValidationResponse passedValidation() {
        RiskValidationResponse.passed()
    }

    private Loan saveLoan(Client loanClient, Status loanStatus, ZonedDateTime createdAt = noon) {
        Loan saved = loanRepository.save(new Loan().tap {
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
            client = loanClient
            status = loanStatus
        })
        transactionTemplate.executeWithoutResult {
            jdbcTemplate.update(
                'update loan set created_at = ? where id = ?', Timestamp.from(createdAt.toInstant()), saved.id)
        }
        loanRepository.findById(saved.id).get()
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
