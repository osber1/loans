package io.osvaldas.backoffice.repositories

import static io.osvaldas.api.clients.Status.DELETED

import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.backoffice.repositories.entities.Client
import spock.lang.Subject

class ClientRepositorySpec extends AbstractDatabaseSpec {

    @Autowired
    LoanRepository loanRepository

    @Subject
    @Autowired
    ClientRepository repository

    void setup() {
        client.addLoan(loan)
        repository.save(client)
    }

    void 'should return user if it exists'() {
        when:
            Optional<Client> client = repository.findById(clientId)
        then:
            client.isPresent() == result
        where:
            clientId        || result
            validClientId   || true
            invalidClientId || false
    }

    void 'should return #result when personal code is #personalCode'() {
        when:
            boolean exists = repository.existsByPersonalCode(personalCode)
        then:
            exists == result
        where:
            personalCode        || result
            validPersonalCode   || true
            invalidPersonalCode || false
    }

    void 'should change client status to deleted when deleting client'() {
        when:
            repository.changeClientStatus(validClientId, DELETED)
        then:
            repository.findById(validClientId).get().status == DELETED
    }

    void 'should return #takenLoans when getting loan count taken today'() {
        when:
            int loansTakenTodayCount = repository.countByIdAndLoansCreatedAtAfter(validClientId, todaysDate)
        then:
            loansTakenTodayCount == takenLoans
        where:
            todaysDate || takenLoans
            date       || 1
            futureDate || 0
    }

}
