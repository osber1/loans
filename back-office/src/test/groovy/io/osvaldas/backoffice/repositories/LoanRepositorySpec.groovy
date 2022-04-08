package io.osvaldas.backoffice.repositories

import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared
import spock.lang.Subject

class LoanRepositorySpec extends AbstractDatabaseSpec {

    @Shared
    int invalidLoanId = 4758758

    @Shared
    Loan loan = createLoan()

    @Subject
    @Autowired
    LoanRepository repository

    void 'should not return loan when it exists'() {
        given:
            Loan savedLoan = repository.save(loan)
        when:
            Optional<Loan> loan = repository.findById(savedLoan.id)
        then:
            loan.isPresent()
    }

    void 'should not return loan when it does not exist'() {
        when:
            Optional<Loan> loan = repository.findById(invalidLoanId)
        then:
            loan.isEmpty()
    }

    private Loan createLoan() {
        return new Loan().tap {
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
        }
    }
}
