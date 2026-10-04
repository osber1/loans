package io.osvaldas.backoffice.repositories

import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.REJECTED
import static java.time.ZonedDateTime.now

import java.time.ZonedDateTime

import org.hibernate.Hibernate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Limit
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
            List<Loan> loans = findNotEvaluated(now().plusMinutes(1))
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
            findNotEvaluated(now().minusMinutes(1)).empty
            repository.findAllWithClientByStatusAndCreatedAtBeforeAndIdGreaterThanOrderByIdAsc(
                REJECTED, now().plusMinutes(1), 0, Limit.of(10)).empty
            findNotEvaluated(now().plusMinutes(1)).size() == 1
    }

    void 'should fetch the next batch of loans in id order after the given id'() {
        given:
            Client savedClient = entityManager.persist(client)
            List<Loan> saved = (1..5).collect { saveLoan(savedClient, NOT_EVALUATED) }
            entityManager.flush()
            entityManager.clear()
        expect:
            findNotEvaluated(now().plusMinutes(1), 0, 2)*.id == saved[0..1]*.id
            findNotEvaluated(now().plusMinutes(1), saved[1].id, 2)*.id == saved[2..3]*.id
            findNotEvaluated(now().plusMinutes(1), saved[3].id, 2)*.id == [saved[4].id]
            findNotEvaluated(now().plusMinutes(1), saved[4].id, 2).empty
    }

    private List<Loan> findNotEvaluated(ZonedDateTime createdBefore, long afterId = 0, int limit = 10) {
        repository.findAllWithClientByStatusAndCreatedAtBeforeAndIdGreaterThanOrderByIdAsc(
            NOT_EVALUATED, createdBefore, afterId, Limit.of(limit))
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
