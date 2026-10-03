package io.osvaldas.backoffice.repositories.specifications

import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING

import java.time.ZonedDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager

import io.osvaldas.backoffice.repositories.AbstractDatabaseSpec
import io.osvaldas.backoffice.repositories.LoanRepository
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Subject

class LoanSpecificationsSpec extends AbstractDatabaseSpec {

    @Subject
    @Autowired
    LoanRepository repository

    @Autowired
    TestEntityManager entityManager

    void setup() {
        loan.client = entityManager.persist(client)
        repository.save(loan)
    }

    void 'should return list size of #listSize when client id is #clientId'() {
        when:
            List<Loan> loans = repository.findAll(LoanSpecifications.clientIdIs(clientId))
        then:
            loans.size() == listSize
        where:
            clientId          || listSize
            VALID_CLIENT_ID   || 1
            INVALID_CLIENT_ID || 0
    }

    void 'should return list size of #listSize when date is #creationDate'() {
        when:
            List<Loan> loans = repository.findAll(LoanSpecifications.loanCreatedAtOrAfter(creationDate))
        then:
            loans.size() == listSize
        where:
            creationDate || listSize
            DATE         || 1
            FUTURE_DATE  || 0
    }

    void 'should include loan created exactly at the given date'() {
        given:
            entityManager.flush()
            entityManager.clear()
            ZonedDateTime createdAt = repository.findById(loan.id).get().createdAt
        expect:
            repository.count(LoanSpecifications.loanCreatedAtOrAfter(createdAt)) == 1
    }

    void 'should return list size of #listSize when statuses are #statuses'() {
        when:
            List<Loan> loans = repository.findAll(LoanSpecifications.loanStatusIn(statuses))
        then:
            loans.size() == listSize
        where:
            statuses                 || listSize
            [OPEN]                   || 1
            [PENDING, NOT_EVALUATED] || 0
    }

    void 'should return list size of #listSize when id is below #description'() {
        when:
            List<Loan> loans = repository.findAll(LoanSpecifications.loanIdLessThan(loan.id + offset))
        then:
            loans.size() == listSize
        where:
            offset || listSize | description
            1      || 1        | 'next loan id'
            0      || 0        | 'loan own id'
    }

}
