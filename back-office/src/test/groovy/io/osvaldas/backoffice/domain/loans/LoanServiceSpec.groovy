package io.osvaldas.backoffice.domain.loans

import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING
import static io.osvaldas.api.loans.Status.REJECTED
import static java.util.Optional.empty
import static java.util.Optional.of

import java.time.Duration

import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.data.jpa.domain.Specification

import io.osvaldas.api.exceptions.BadRequestException
import io.osvaldas.api.exceptions.ClientNotActiveException
import io.osvaldas.api.exceptions.NotFoundException
import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.api.risk.validation.RiskRejectionReason
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.api.util.TimeUtils
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.clients.ClientService
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig
import io.osvaldas.backoffice.repositories.LoanRepository
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Subject

class LoanServiceSpec extends AbstractSpec {

    ClientService clientService = Stub()

    TimeUtils timeUtils = Stub {
        currentDateTime >> DATE
        hourOfDay >> 10
    }

    PropertiesConfig config = Stub()

    RiskCheckerClient riskCheckerClient = Mock()

    Cache loanResponseCache = Mock()

    CacheManager cacheManager = Stub {
        getCache('LoanResponse') >> loanResponseCache
    }

    LoanRepository loanRepository = Mock {
        save(_ as Loan) >> loan
        findById(LOAN_ID) >> { of(loan) }
    }

    @Subject
    LoanService loanService = new LoanService(clientService, loanRepository, config, timeUtils, riskCheckerClient,
        cacheManager)

    void setup() {
        loan.status = PENDING
    }

    void 'should save loan'() {
        when:
            loanService.save(loan)
        then:
            1 * loanRepository.save(loan) >> loan
    }

    void 'should return loans list with postpones fetched when there are loans'() {
        given:
            clientService.getClient(CLIENT_ID) >> registeredClientWithLoan
        when:
            Collection loans = loanService.getLoans(CLIENT_ID)
        then:
            loans == [loan]
        and:
            1 * loanRepository.findAllWithPostponesByClientId(CLIENT_ID) >> [loan]
    }

    void 'should return empty list when there are no loans'() {
        given:
            clientService.getClient(CLIENT_ID) >> registeredClientWithId
        when:
            Collection loans = loanService.getLoans(CLIENT_ID)
        then:
            loans.empty
        and:
            1 * loanRepository.findAllWithPostponesByClientId(CLIENT_ID) >> []
    }

    void 'should throw exception when getting loans of non existing client'() {
        given:
            clientService.getClient(CLIENT_ID) >> { throw new NotFoundException(CLIENT_NOT_FOUND) }
        when:
            loanService.getLoans(CLIENT_ID)
        then:
            NotFoundException e = thrown()
            e.message == CLIENT_NOT_FOUND
        and:
            0 * loanRepository.findAllWithPostponesByClientId(_)
    }

    void 'should return loan with postpones fetched when it exists'() {
        when:
            Loan loan = loanService.getLoan(LOAN_ID)
        then:
            loan.id == LOAN_ID
        and:
            1 * loanRepository.findWithPostponesById(LOAN_ID) >> of(loan)
    }

    void 'should throw exception when trying to get non existing loan'() {
        when:
            loanService.getLoan(LOAN_ID)
        then:
            NotFoundException e = thrown()
            e.message == "Loan with id ${LOAN_ID} does not exist."
        and:
            1 * loanRepository.findWithPostponesById(LOAN_ID) >> empty()
    }

    void 'should return locked loan when getting it for update'() {
        when:
            Loan lockedLoan = loanService.getLoanForUpdate(LOAN_ID)
        then:
            lockedLoan == loan
        and:
            1 * loanRepository.findForUpdateById(LOAN_ID) >> of(loan)
    }

    void 'should throw exception when trying to get non existing loan for update'() {
        when:
            loanService.getLoanForUpdate(LOAN_ID)
        then:
            NotFoundException e = thrown()
            e.message == "Loan with id ${LOAN_ID} does not exist."
        and:
            1 * loanRepository.findForUpdateById(LOAN_ID) >> empty()
    }

    void 'should throw exception when amount limit is exceeded'() {
        given:
            clientService.getClientForUpdate(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> RiskValidationResponse.rejected(RiskRejectionReason.AMOUNT_EXCEEDS, AMOUNT_EXCEEDS)
        and:
            Loan addedLoan = loanService.addLoan(buildLoan(1000.00), CLIENT_ID)
        when:
            loanService.validate(addedLoan, CLIENT_ID)
        then:
            ValidationRuleException e = thrown()
            e instanceof ValidationRuleException.AmountException
            e.reason == RiskRejectionReason.AMOUNT_EXCEEDS
            e.message == AMOUNT_EXCEEDS
        and:
            addedLoan.status == REJECTED
    }

    void 'should throw exception when max amount and forbidden time'() {
        given:
            clientService.getClientForUpdate(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> RiskValidationResponse.rejected(RiskRejectionReason.FORBIDDEN_TIME, RISK_TOO_HIGH)
        and:
            Loan addedLoan = loanService.addLoan(loan, CLIENT_ID)
        when:
            loanService.validate(addedLoan, CLIENT_ID)
        then:
            ValidationRuleException e = thrown()
            e instanceof ValidationRuleException.TimeException
            e.reason == RiskRejectionReason.FORBIDDEN_TIME
            e.message == RISK_TOO_HIGH
    }

    void 'should throw exception when too much loans taken today'() {
        given:
            clientService.getClient(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> RiskValidationResponse.rejected(RiskRejectionReason.LOAN_LIMIT_EXCEEDS, LOAN_LIMIT_EXCEEDS)
        when:
            loanService.validate(loan, CLIENT_ID)
        then:
            ValidationRuleException e = thrown()
            e instanceof ValidationRuleException.LoanLimitException
            e.reason == RiskRejectionReason.LOAN_LIMIT_EXCEEDS
            e.message == LOAN_LIMIT_EXCEEDS
    }

    void 'should still reject loan when risk checker gives no reason'() {
        given:
            clientService.getClient(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, null, 'Rejected.')
        when:
            loanService.validate(loan, CLIENT_ID)
        then:
            ValidationRuleException e = thrown()
            e.reason == null
            e.message == 'Rejected.'
        and:
            loan.status == REJECTED
    }

    void 'should throw exception when failed to call feign client'() {
        given:
            clientService.getClientForUpdate(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest) >> { throw new BadRequestException('') }
        and:
            Loan addedLoan = loanService.addLoan(loan, CLIENT_ID)
        when:
            loanService.validate(addedLoan, CLIENT_ID)
        then:
            addedLoan.status == NOT_EVALUATED
        and:
            BadRequestException e = thrown()
            e.message == ''
        and:
            1 * loanResponseCache.evict(LOAN_ID)
    }

    void 'should take loan when validation pass'() {
        given:
            clientService.getClientForUpdate(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> RiskValidationResponse.passed()
        when:
            Loan takenLoan = loanService.addLoan(loan, CLIENT_ID)
            loanService.validate(takenLoan, CLIENT_ID)
        then:
            takenLoan == loan
        and:
            takenLoan.status == OPEN
    }

    void 'should throw exception when client is not active'() {
        given:
            clientService.getClientForUpdate(CLIENT_ID) >> registeredClientWithId
        when:
            loanService.addLoan(loan, CLIENT_ID)
        then:
            ClientNotActiveException e = thrown()
            e.message == CLIENT_NOT_ACTIVE
    }

    void 'should send loans taken today with validation request'() {
        when:
            loanService.validate(loan, CLIENT_ID)
        then:
            1 * loanRepository.count(_ as Specification) >> 3L
            0 * loanRepository.findAll(_ as Specification)
        then:
            1 * riskCheckerClient.validate(new RiskValidationRequest(LOAN_ID, CLIENT_ID, loan.amount, 3L))
                >> RiskValidationResponse.passed()
    }

    void 'should return #result.size() loans when status is #status'() {
        given:
            1 * loanRepository.findAllWithClientByStatusAndCreatedAtBefore(status, DATE.minusMinutes(5)) >> result
        expect:
            loanService.getLoansByStatusOlderThan(status, Duration.ofMinutes(5)) == result
        where:
            result             | status
            []                 | NOT_EVALUATED
            [buildLoan(100.0)] | OPEN
    }

}
