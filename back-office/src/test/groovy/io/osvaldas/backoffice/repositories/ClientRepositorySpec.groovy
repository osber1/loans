package io.osvaldas.backoffice.repositories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException

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

    void 'should reject client with already registered personal code'() {
        given:
            Client duplicate = new Client().tap {
                id = INVALID_CLIENT_ID
                firstName = 'Other'
                lastName = 'User'
                email = 'other@mail.com'
                phoneNumber = '+37062541366'
                personalCode = VALID_PERSONAL_CODE
            }
        when:
            repository.saveAndFlush(duplicate)
        then:
            DataIntegrityViolationException e = thrown()
            e.message.contains('uk_client_personal_code')
    }

}
