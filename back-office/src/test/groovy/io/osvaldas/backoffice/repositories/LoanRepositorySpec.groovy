package io.osvaldas.backoffice.repositories

import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING
import static java.time.ZonedDateTime.now

import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager

import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Subject

class LoanRepositorySpec extends AbstractDatabaseSpec {

    @Subject
    @Autowired
    LoanRepository repository

    @Autowired
    TestEntityManager entityManager

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

    void 'should fetch postpones together with loan'() {
        given:
            Client savedClient = entityManager.persist(client)
            loan.client = savedClient
            loan.returnDate = DATE
            loan.postponeLoan(7, 1.5)
            Loan savedLoan = repository.save(loan)
            entityManager.flush()
            entityManager.clear()
        when:
            Loan foundLoan = repository.findWithPostponesById(savedLoan.id).get()
            List<Loan> clientLoans = repository.findAllWithPostponesByClientId(VALID_CLIENT_ID)
        then:
            Hibernate.isInitialized(foundLoan.loanPostpones)
            foundLoan.loanPostpones.size() == 1
        and:
            clientLoans.size() == 1
            Hibernate.isInitialized(clientLoans.first().loanPostpones)
    }

    void 'should fetch client together with loans by status'() {
        given:
            saveLoan(entityManager.persist(client), NOT_EVALUATED)
            entityManager.flush()
            entityManager.clear()
        when:
            List<Loan> loans =
                repository.findAllWithClientByStatusAndCreatedAtBefore(NOT_EVALUATED, now().plusMinutes(1))
        then:
            loans.size() == 1
            Hibernate.isInitialized(loans.first().client)
            loans.first().client.id == VALID_CLIENT_ID
    }

    void 'should not fetch loans created after the given time or with another status'() {
        given:
            Client savedClient = entityManager.persist(client)
            saveLoan(savedClient, NOT_EVALUATED)
            saveLoan(savedClient, OPEN)
            entityManager.flush()
            entityManager.clear()
        expect:
            repository.findAllWithClientByStatusAndCreatedAtBefore(NOT_EVALUATED, now().minusMinutes(1)).empty
            repository.findAllWithClientByStatusAndCreatedAtBefore(PENDING, now().plusMinutes(1)).empty
            repository.findAllWithClientByStatusAndCreatedAtBefore(NOT_EVALUATED, now().plusMinutes(1)).size() == 1
    }

    private Loan saveLoan(Client loanClient, io.osvaldas.api.loans.Status loanStatus) {
        repository.save(new Loan().tap {
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
            client = loanClient
            status = loanStatus
        })
    }

}
