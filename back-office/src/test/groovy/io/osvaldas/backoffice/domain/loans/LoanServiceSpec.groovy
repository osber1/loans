package io.osvaldas.backoffice.domain.loans

import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING
import static io.osvaldas.api.loans.Status.REJECTED
import static java.util.Optional.empty
import static java.util.Optional.of

import org.springframework.data.jpa.domain.Specification
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate

import io.osvaldas.api.exceptions.BadRequestException
import io.osvaldas.api.exceptions.ClientNotActiveException
import io.osvaldas.api.exceptions.NotFoundException
import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.loans.TodayTakenLoansCount
import io.osvaldas.api.risk.validation.RiskValidationRequest
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

    LoanRepository loanRepository = Mock {
        save(_ as Loan) >> loan
        findById(LOAN_ID) >> { of(loan) }
        findFirstByClientIdOrderByIdDesc(CLIENT_ID) >> empty()
    }

    TransactionTemplate transactionTemplate = Stub {
        executeWithoutResult(_) >> { args -> args[0].accept(Stub(TransactionStatus)) }
    }

    @Subject
    LoanService loanService = new LoanService(clientService, loanRepository, config, timeUtils,
        riskCheckerClient, transactionTemplate)

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
            clientService.getClient(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, AMOUNT_EXCEEDS)
        and:
            Loan addedLoan = loanService.addLoan(buildLoan(1000.00), CLIENT_ID)
        when:
            loanService.validate(addedLoan, CLIENT_ID)
        then:
            ValidationRuleException e = thrown()
            e.message == AMOUNT_EXCEEDS
        and:
            addedLoan.status == REJECTED
    }

    void 'should throw exception when max amount and forbidden time'() {
        given:
            clientService.getClient(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, RISK_TOO_HIGH)
        and:
            Loan addedLoan = loanService.addLoan(loan, CLIENT_ID)
        when:
            loanService.validate(addedLoan, CLIENT_ID)
        then:
            ValidationRuleException e = thrown()
            e.message == RISK_TOO_HIGH
    }

    void 'should throw exception when too much loans taken today'() {
        given:
            clientService.getClient(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, LOAN_LIMIT_EXCEEDS)
        when:
            loanService.validate(loan, CLIENT_ID)
        then:
            ValidationRuleException e = thrown()
            e.message == LOAN_LIMIT_EXCEEDS
    }

    void 'should throw exception when failed to call feign client'() {
        given:
            clientService.getClient(CLIENT_ID) >> activeClientWithId
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
    }

    void 'should take loan when validation pass'() {
        given:
            clientService.getClient(CLIENT_ID) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(true, 'Risk validation passed.')
        when:
            Loan takenLoan = loanService.addLoan(loan, CLIENT_ID)
            loanService.validate(takenLoan, CLIENT_ID)
        then:
            takenLoan == loan
        and:
            takenLoan.status == OPEN
    }

    void 'should change status of managed loan without merging the passed instance'() {
        given:
            Loan detachedLoan = buildLoan(100.0, PENDING)
            Loan managedLoan = buildLoan(100.0, PENDING)
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(true, 'Risk validation passed.')
        when:
            loanService.validate(detachedLoan, CLIENT_ID)
        then:
            2 * loanRepository.findById(LOAN_ID) >> of(managedLoan)
            0 * loanRepository.save(_)
        and:
            managedLoan.status == OPEN
            detachedLoan.status == OPEN
    }

    void 'should not evaluate loan when its status was changed concurrently'() {
        given:
            Loan staleLoan = buildLoan(100.0, PENDING)
        when:
            loanService.validate(staleLoan, CLIENT_ID)
        then:
            1 * loanRepository.findById(LOAN_ID) >> of(buildLoan(100.0, OPEN))
            0 * riskCheckerClient.validate(_)
        and:
            BadRequestException e = thrown()
            e.message == LoanService.LOAN_STATUS_CHANGED.formatted(LOAN_ID, OPEN, [PENDING, NOT_EVALUATED])
        and:
            staleLoan.status == PENDING
    }

    void 'should reject last pending loan when new is taken'() {
        given:
            Loan pendingLoan = buildLoan(100.0, PENDING)
            clientService.getClient(CLIENT_ID) >> activeClientWithId
        when:
            loanService.addLoan(buildLoanWithoutId(100.0), CLIENT_ID)
        then:
            1 * loanRepository.findFirstByClientIdOrderByIdDesc(CLIENT_ID) >> of(pendingLoan)
        and:
            pendingLoan.status == REJECTED
    }

    void 'should not reject last loan when it is not pending'() {
        given:
            Loan openLoan = buildLoan(100.0, OPEN)
            clientService.getClient(CLIENT_ID) >> activeClientWithId
        when:
            loanService.addLoan(buildLoanWithoutId(100.0), CLIENT_ID)
        then:
            1 * loanRepository.findFirstByClientIdOrderByIdDesc(CLIENT_ID) >> of(openLoan)
        and:
            openLoan.status == OPEN
    }

    void 'should throw exception when client is not active'() {
        given:
            clientService.getClient(CLIENT_ID) >> registeredClientWithId
        when:
            loanService.addLoan(loan, CLIENT_ID)
        then:
            ClientNotActiveException e = thrown()
            e.message == CLIENT_NOT_ACTIVE
    }

    void 'should get today taken loans count'() {
        when:
            TodayTakenLoansCount todayTakenLoansCount = loanService.getTodayTakenLoansCount(CLIENT_ID)
        then:
            todayTakenLoansCount.takenLoansCount() == 3
        and:
            1 * loanRepository.count(_ as Specification) >> 3L
            0 * loanRepository.findAll(_ as Specification)
    }

    void 'should return #result.size() loans when status is #status'() {
        given:
            1 * loanRepository.findAllWithClientByStatus(status) >> result
        expect:
            loanService.getLoansByStatus(status) == result
        where:
            result             | status
            []                 | NOT_EVALUATED
            [buildLoan(100.0)] | OPEN
    }

}
