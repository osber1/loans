package io.osvaldas.backoffice.repositories.specifications

import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.PENDING
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.clientIdIs
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanCreationDateIsAfter
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanStatusIs
import static org.springframework.data.jpa.domain.Specification.where

import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.backoffice.repositories.AbstractDatabaseSpec
import io.osvaldas.backoffice.repositories.LoanRepository
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Subject

class LoanSpecificationsSpec extends AbstractDatabaseSpec {

    @Subject
    @Autowired
    LoanRepository repository

    void setup() {
        loan.client = client
        repository.save(loan)
    }

    void 'should return list size of #listSize when client id is #clientId'() {
        when:
            List<Loan> loans = repository.findAll(where(clientIdIs(clientId)))
        then:
            loans.size() == listSize
        where:
            clientId        || listSize
            validClientId   || 1
            invalidClientId || 0
    }

    void 'should return list size of #listSize when date is #creationDate'() {
        when:
            List<Loan> loans = repository.findAll(where(loanCreationDateIsAfter(creationDate)))
        then:
            loans.size() == listSize
        where:
            creationDate || listSize
            date         || 1
            futureDate   || 0
    }

    void 'should return list size of #listSize when status is #status'() {
        when:
            List<Loan> loans = repository.findAll(where(loanStatusIs(status)))
        then:
            loans.size() == listSize
        where:
            status  || listSize
            OPEN    || 1
            PENDING || 0
    }

}
