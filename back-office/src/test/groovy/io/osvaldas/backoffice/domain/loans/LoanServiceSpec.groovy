package io.osvaldas.backoffice.domain.loans

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING
import static io.osvaldas.api.loans.Status.REJECTED
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
import io.osvaldas.backoffice.repositories.specifications.LoanSpecifications
import spock.lang.Subject

class LoanServiceSpec extends AbstractSpec {

    ClientService clientService = Stub()

    TimeUtils timeUtils = Stub {
        currentDateTime >> date
        hourOfDay >> 10
    }

    PropertiesConfig config = Stub()

    RiskCheckerClient riskCheckerClient = Stub()

    LoanRepository loanRepository = Mock {
        save(_ as Loan) >> loan
    }

    @Subject
    LoanService loanService = new LoanService(clientService, loanRepository, config, timeUtils, riskCheckerClient)

    void setup() {
        loan.id = 1
    }

    void 'should save loan'() {
        when:
            loanService.save(loan)
        then:
            1 * loanRepository.save(loan) >> loan
    }

    void 'should return loans list when there are loans'() {
        given:
            clientService.getClient(clientId) >> registeredClientWithLoan
        when:
            Collection loans = loanService.getLoans(clientId)
        then:
            loans.size() == 1
        and:
            loans == [loan] as Set
    }

    void 'should return empty list when there are no loans'() {
        given:
            clientService.getClient(clientId) >> registeredClientWithId
        when:
            Collection loans = loanService.getLoans(clientId)
        then:
            loans == emptySet()
    }

    void 'should return loan when it exists'() {
        when:
            Loan loan = loanService.getLoan(loanId)
        then:
            loan.id == loanId
        and:
            1 * loanRepository.findById(loanId) >> of(loan)
    }

    void 'should throw exception when trying to get non existing loan'() {
        when:
            loanService.getLoan(loanId)
        then:
            NotFoundException e = thrown()
            e.message == "Loan with id ${loanId} does not exist."
        and:
            1 * loanRepository.findById(loanId) >> empty()
    }

    void 'should throw exception when amount limit is exceeded'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, amountExceeds)
        and:
            Loan addedLoan = loanService.addLoan(buildLoan(1000.00), clientId)
        when:
            loanService.validate(addedLoan, clientId)
        then:
            ValidationRuleException e = thrown()
            e.message == amountExceeds
    }

    void 'should throw exception when max amount and forbidden time'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, riskTooHigh)
        and:
            Loan addedLoan = loanService.addLoan(loan, clientId)
        when:
            loanService.validate(addedLoan, clientId)
        then:
            ValidationRuleException e = thrown()
            e.message == riskTooHigh
    }

    void 'should throw exception when too much loans taken today'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, loanLimitExceeds)
        when:
            loanService.validate(loan, clientId)
        then:
            ValidationRuleException e = thrown()
            e.message == loanLimitExceeds
    }

    void 'should throw exception when failed to call feign client'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest) >> { throw new BadRequestException('') }
        and:
            Loan addedLoan = loanService.addLoan(loan, clientId)
        when:
            loanService.validate(addedLoan, clientId)
        then:
            addedLoan.status == NOT_EVALUATED
        and:
            BadRequestException e = thrown()
            e.message == ''
    }

    void 'should take loan when validation pass'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
            clientService.save(activeClientWithId) >> activeClientWithLoan
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(true, 'Risk validation passed.')
        when:
            Loan takenLoan = loanService.addLoan(loan, clientId)
            loanService.validate(takenLoan, clientId)
        then:
            takenLoan == loan
    }

    void 'should reject last pending loan when new is taken'() {
        given:
            loan.status = PENDING
        and:
            Client client = buildClient(clientId, [loan] as Set, ACTIVE)
            clientService.getClient(clientId) >> client
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest) >> new RiskValidationResponse(true, 'Risk validation passed.')
        when:
            loanService.addLoan(loan, clientId)
        then:
            REJECTED == client.loans.findAll { it.id == 1 }.first().status
    }

    void 'should throw exception when client is not active'() {
        given:
            clientService.getClient(clientId) >> registeredClientWithId
        when:
            loanService.addLoan(loan, clientId)
        then:
            ClientNotActiveException e = thrown()
            e.message == clientNotActive
    }

    void 'should get today taken loans count'() {
        when:
            TodayTakenLoansCount todayTakenLoansCount = loanService.getTodayTakenLoansCount(clientId)
        then:
            todayTakenLoansCount.takenLoansCount() == 1
        and:
            1 * loanRepository.findAll(_ as Specification) >> [loan]
    }

    void 'should return #result.size() loans when status is #status'() {
        given:
            1 * loanRepository.findAll { Specification s ->
                with(s) {
                    Specification.where(LoanSpecifications.loanStatusIs(status))
                }
            } >> result
        expect:
            loanService.getLoansByStatus(status) == result
        where:
            result | status
            []     | NOT_EVALUATED
            [loan] | OPEN
    }

}
