package io.osvaldas.backoffice.repositories

import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Subject

class LoanRepositorySpec extends AbstractDatabaseSpec {

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
            Optional<Loan> loan = repository.findById(INVALID_LOAN_ID)
        then:
            loan.isEmpty()
    }

}
