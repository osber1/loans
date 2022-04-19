package io.osvaldas.backoffice.domain.loans

import static java.util.Collections.emptySet
import static java.util.Optional.empty
import static java.util.Optional.of

import java.time.ZonedDateTime

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

    ClientService clientService = Stub {
        save(activeClientWithId) >> activeClientWithLoan
    }

    TimeUtils timeUtils = Stub {
        currentDateTime >> date
        hourOfDay >> 10
    }

    PropertiesConfig config = Stub {
        maxAmount >> 100.00
        forbiddenHourFrom >> 0
        forbiddenHourTo >> 6
    }

    RiskCheckerClient riskCheckerClient = Stub()

    LoanRepository loanRepository = Mock()

    @Subject
    LoanService loanService = new LoanService(clientService, loanRepository, config, timeUtils, riskCheckerClient)

    void setup() {
        loanService.loanErrorMessage = loanErrorMessage
        loanService.clientNotActiveMessage = clientNotActiveMessage
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
            loans == Set.of(loan)
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
            e.message == loanErrorMessage
        and:
            1 * loanRepository.findById(loanId) >> empty()
    }

    void 'should throw exception when amount limit is exceeded'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, amountExceedsMessage)
        when:
            Loan addedLoan = loanService.addLoan(buildLoan(1000.00), clientId)
        and:
            loanService.validate(addedLoan, clientId)
        then:
            ValidationRuleException e = thrown()
            e.message == amountExceedsMessage
    }

    void 'should throw exception when max amount and forbidden time'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, riskMessage)
        when:
            Loan addedLoan = loanService.addLoan(loan, clientId)
        and:
            loanService.validate(addedLoan, clientId)
        then:
            ValidationRuleException e = thrown()
            e.message == riskMessage
    }

    void 'should throw exception when too much loans taken today'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        and:
            riskCheckerClient.validate(_ as RiskValidationRequest)
                >> new RiskValidationResponse(false, loanLimitExceedsMessage)
        when:
            Loan addedLoan = loanService.addLoan(loan, clientId)
        and:
            loanService.validate(addedLoan, clientId)
        then:
            ValidationRuleException e = thrown()
            e.message == loanLimitExceedsMessage
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
        then:
            takenLoan == loan
    }

    void 'should throw exception when client is not active'() {
        given:
            clientService.getClient(clientId) >> registeredClientWithId
        when:
            loanService.addLoan(loan, clientId)
        then:
            ClientNotActiveException e = thrown()
            e.message == clientNotActiveMessage
    }

    void 'should get today taken loans count'() {
        given:
            clientService.getLoanTakenTodayCount(clientId, _ as ZonedDateTime) >> 1
        when:
            TodayTakenLoansCount todayTakenLoansCount = loanService.getTodayTakenLoansCount(clientId)
        then:
            todayTakenLoansCount.takenLoansCount == 1
    }

}
