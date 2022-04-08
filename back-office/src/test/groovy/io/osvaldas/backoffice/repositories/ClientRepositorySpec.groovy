package io.osvaldas.backoffice.repositories

import static io.osvaldas.backoffice.repositories.entities.Status.DELETED

import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.backoffice.repositories.entities.Client
import spock.lang.Shared
import spock.lang.Subject

class ClientRepositorySpec extends AbstractDatabaseSpec {

    @Shared
    String invalidClientId = 'invalidClientId'

    @Shared
    String validClientId = 'validClientId'

    @Shared
    long validPersonalCode = 12345678911

    @Shared
    long invalidPersonalCode = 12345678910

    @Shared
    Client client = createClient()

    @Subject
    @Autowired
    ClientRepository repository

    void setup() {
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

    private Client createClient() {
        return new Client().tap {
            id = validClientId
            firstName = 'Test'
            lastName = 'User'
            email = 'user@mail.com'
            phoneNumber = '+37062541365'
            personalCode = validPersonalCode
        }
    }
}
