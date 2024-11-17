package io.osvaldas.backoffice.repositories

import static io.osvaldas.api.clients.Status.DELETED

import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.backoffice.repositories.entities.Client
import spock.lang.Subject

class ClientRepositorySpec extends AbstractDatabaseSpec {

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
            client.present == result
        where:
            clientId          || result
            VALID_CLIENT_ID   || true
            INVALID_CLIENT_ID || false
    }

    void 'should return #result when personal code is #personalCode'() {
        when:
            boolean exists = repository.existsByPersonalCode(personalCode)
        then:
            exists == result
        where:
            personalCode          || result
            VALID_PERSONAL_CODE   || true
            INVALID_PERSONAL_CODE || false
    }

    void 'should change client status to deleted when deleting client'() {
        when:
            repository.changeClientStatus(VALID_CLIENT_ID, DELETED)
        then:
            repository.findById(VALID_CLIENT_ID).get().status == DELETED
    }

}
