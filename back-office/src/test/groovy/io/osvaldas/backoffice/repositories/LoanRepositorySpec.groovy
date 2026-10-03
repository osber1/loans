package io.osvaldas.backoffice.repositories

import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING

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

    void 'should increment version when loan is updated'() {
        given:
            Loan savedLoan = repository.saveAndFlush(loan)
            Long initialVersion = savedLoan.version
        when:
            savedLoan.status = NOT_EVALUATED
            repository.saveAndFlush(savedLoan)
        then:
            initialVersion == 0
            savedLoan.version == initialVersion + 1
    }

    void 'should return client last loan'() {
        given:
            Client savedClient = entityManager.persist(client)
            Loan firstLoan = saveLoan(savedClient, OPEN)
            Loan lastLoan = saveLoan(savedClient, PENDING)
        expect:
            repository.findFirstByClientIdOrderByIdDesc(VALID_CLIENT_ID).get().id == lastLoan.id
            firstLoan.id < lastLoan.id
        and:
            repository.findFirstByClientIdOrderByIdDesc(INVALID_CLIENT_ID).isEmpty()
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
            List<Loan> loans = repository.findAllWithClientByStatus(NOT_EVALUATED)
        then:
            loans.size() == 1
            Hibernate.isInitialized(loans.first().client)
            loans.first().client.id == VALID_CLIENT_ID
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
