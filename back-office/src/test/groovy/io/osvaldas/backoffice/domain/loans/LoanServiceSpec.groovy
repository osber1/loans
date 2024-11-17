package io.osvaldas.backoffice.domain.loans

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING
import static io.osvaldas.api.loans.Status.REJECTED
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanStatusIs
import static java.util.Collections.emptySet
import static java.util.Optional.empty
import static java.util.Optional.of

import org.springframework.data.jpa.domain.Specification

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
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Subject

class LoanServiceSpec extends AbstractSpec {

    ClientService clientService = Stub()

    TimeUtils timeUtils = Stub {
        currentDateTime >> DATE
        hourOfDay >> 10
    }

    PropertiesConfig config = Stub()

    RiskCheckerClient riskCheckerClient = Stub()

    LoanRepository loanRepository = Mock {
        save(_ as Loan) >> loan
    }

    @Subject
    LoanService loanService = new LoanService(clientService, loanRepository, config, timeUtils, riskCheckerClient)

    void 'should save loan'() {
        when:
            loanService.save(loan)
        then:
            1 * loanRepository.save(loan) >> loan
    }

    void 'should return loans list when there are loans'() {
        given:
            clientService.getClient(CLIENT_ID) >> registeredClientWithLoan
        when:
            Collection loans = loanService.getLoans(CLIENT_ID)
        then:
            loans.size() == 1
        and:
            loans == [loan] as Set
    }

    void 'should return empty list when there are no loans'() {
        given:
            clientService.getClient(CLIENT_ID) >> registeredClientWithId
        when:
            Collection loans = loanService.getLoans(CLIENT_ID)
        then:
            loans == emptySet()
    }

    void 'should return loan when it exists'() {
        when:
            Loan loan = loanService.getLoan(LOAN_ID)
        then:
            loan.id == LOAN_ID
        and:
            1 * loanRepository.findById(LOAN_ID) >> of(loan)
    }

    void 'should throw exception when trying to get non existing loan'() {
        when:
            loanService.getLoan(LOAN_ID)
        then:
            NotFoundException e = thrown()
            e.message == "Loan with id ${LOAN_ID} does not exist."
        and:
            1 * loanRepository.findById(LOAN_ID) >> empty()
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
            clientService.save(activeClientWithId) >> activeClientWithLoan
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(true, 'Risk validation passed.')
        when:
            Loan takenLoan = loanService.addLoan(loan, CLIENT_ID)
            loanService.validate(takenLoan, CLIENT_ID)
        then:
            takenLoan == loan
    }

    void 'should reject last pending loan when new is taken'() {
        given:
            Client client = buildClient(CLIENT_ID, [(buildLoan(100.0, PENDING))] as Set, ACTIVE)
            clientService.getClient(CLIENT_ID) >> client
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(true, 'Risk validation passed.')
        when:
            loanService.addLoan(loan, CLIENT_ID)
        then:
            REJECTED == client.loans.findAll { it.id == 1 }.first().status
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
            todayTakenLoansCount.takenLoansCount() == 1
        and:
            1 * loanRepository.findAll(_ as Specification) >> [loan]
    }

    void 'should return #result.size() loans when status is #status'() {
        given:
            1 * loanRepository.findAll { Specification s ->
                with(s) {
                    loanStatusIs(status)
                }
            } >> result
        expect:
            loanService.getLoansByStatus(status) == result
        where:
            result             | status
            []                 | NOT_EVALUATED
            [buildLoan(100.0)] | OPEN
    }

}
