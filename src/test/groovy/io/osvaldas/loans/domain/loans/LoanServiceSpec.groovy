package io.osvaldas.loans.domain.loans

import static java.util.Collections.emptySet
import static java.util.Collections.singletonList
import static java.util.Optional.empty
import static java.util.Optional.of

import io.osvaldas.loans.domain.AbstractServiceSpec
import io.osvaldas.loans.domain.clients.ClientService
import io.osvaldas.loans.domain.exceptions.NotFoundException
import io.osvaldas.loans.domain.loans.validators.RiskValidator
import io.osvaldas.loans.domain.loans.validators.TimeAndAmountValidator
import io.osvaldas.loans.domain.loans.validators.TimeAndAmountValidator.AmountException
import io.osvaldas.loans.domain.loans.validators.TimeAndAmountValidator.TimeException
import io.osvaldas.loans.domain.util.TimeUtils
import io.osvaldas.loans.infra.configuration.PropertiesConfig
import io.osvaldas.loans.repositories.LoanRepository
import io.osvaldas.loans.repositories.entities.Loan

class LoanServiceSpec extends AbstractServiceSpec {

    ClientService clientService = Stub {
        save(clientWithId) >> clientWithLoan
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

    TimeAndAmountValidator timeAndAmountValidator = new TimeAndAmountValidator(riskMessage, amountExceedsMessage, config, timeUtils)

    RiskValidator validator = new RiskValidator(singletonList(timeAndAmountValidator))

    LoanRepository loanRepository = Mock()

    LoanService loanService = new LoanService(clientService, loanRepository, config, timeUtils, validator, loanErrorMessage)

    void 'should save loan'() {
        when:
            loanService.save(loan)
        then:
            1 * loanRepository.save(loan) >> loan
    }

    void 'should return loans list when there are loans'() {
        given:
            clientService.getClient(clientId) >> clientWithLoan
        when:
            Collection loans = loanService.getLoans(clientId)
        then:
            loans.size() == 1
        and:
            loans == Set.of(loan)
    }

    void 'should return empty list when there are no loans'() {
        given:
            clientService.getClient(clientId) >> clientWithId
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
        when:
            loanService.takeLoan(buildLoan(1000.00), clientId)
        then:
            AmountException exception = thrown()
            exception.message == amountExceedsMessage
    }

    void 'should throw exception when max amount and forbidden time'() {
        when:
            loanService.takeLoan(loan, clientId)
        then:
            timeUtils.getHourOfDay() >> 4
        and:
            TimeException exception = thrown()
            exception.message == 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'
    }

    void 'should take loan when validation pass'() {
        given:
            clientService.getClient(clientId) >> clientWithId
        when:
            Loan takenLoan = loanService.takeLoan(loan, clientId)
        then:
            takenLoan == loan
    }
}
