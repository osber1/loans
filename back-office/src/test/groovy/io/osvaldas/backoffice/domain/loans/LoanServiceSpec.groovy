package io.osvaldas.backoffice.domain.loans

import static java.util.Collections.emptySet
import static java.util.Collections.singletonList
import static java.util.Optional.empty
import static java.util.Optional.of

import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.clients.ClientService
import io.osvaldas.backoffice.domain.exceptions.ClientNotActiveException
import io.osvaldas.backoffice.domain.exceptions.NotFoundException
import io.osvaldas.backoffice.domain.loans.validators.RiskValidator
import io.osvaldas.backoffice.domain.loans.validators.TimeAndAmountValidator
import io.osvaldas.backoffice.domain.loans.validators.TimeAndAmountValidator.AmountException
import io.osvaldas.backoffice.domain.loans.validators.TimeAndAmountValidator.TimeException
import io.osvaldas.backoffice.domain.util.TimeUtils
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
        requestsFromSameIpLimit >> 3
        maxAmount >> 100.00
        forbiddenHourFrom >> 0
        forbiddenHourTo >> 6
    }

    TimeAndAmountValidator timeAndAmountValidator = new TimeAndAmountValidator(config, timeUtils)

    RiskValidator validator = new RiskValidator(singletonList(timeAndAmountValidator))

    LoanRepository loanRepository = Mock()

    @Subject
    LoanService loanService = new LoanService(clientService, loanRepository, config, timeUtils, validator)

    void setup() {
        timeAndAmountValidator.amountExceedsMessage = amountExceedsMessage
        timeAndAmountValidator.riskMessage = riskMessage
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
        when:
            loanService.takeLoan(buildLoan(1000.00), clientId)
        then:
            AmountException e = thrown()
            e.message == amountExceedsMessage
    }

    void 'should throw exception when max amount and forbidden time'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        when:
            loanService.takeLoan(loan, clientId)
        then:
            timeUtils.hourOfDay >> 4
        and:
            TimeException e = thrown()
            e.message == riskMessage
    }

    void 'should take loan when validation pass'() {
        given:
            clientService.getClient(clientId) >> activeClientWithId
        when:
            Loan takenLoan = loanService.takeLoan(loan, clientId)
        then:
            takenLoan == loan
    }

    void 'should throw exception when client is not active'() {
        given:
            clientService.getClient(clientId) >> registeredClientWithId
        when:
            loanService.takeLoan(loan, clientId)
        then:
            ClientNotActiveException e = thrown()
            e.message == clientNotActiveMessage
    }

}
