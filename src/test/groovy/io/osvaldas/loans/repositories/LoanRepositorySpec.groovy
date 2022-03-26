package io.osvaldas.loans.repositories

import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.loans.repositories.entities.Loan
import spock.lang.Shared
import spock.lang.Subject

class LoanRepositorySpec extends AbstractDatabaseSpec {

    @Shared
    int validLoanId = 1

    @Shared
    int invalidLoanId = 4

    @Shared
    Loan loan = createLoan()

    @Subject
    @Autowired
    LoanRepository repository

    void 'should return loan if it exists'() {
        given:
            repository.save(loan)
        when:
            Optional<Loan> loan = repository.findById(loanId)
        then:
            loan.isPresent() == result
        where:
            loanId        || result
            validLoanId   || true
            invalidLoanId || false
    }

    private Loan createLoan() {
        return new Loan().tap {
            id = validLoanId
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
        }
    }
}
