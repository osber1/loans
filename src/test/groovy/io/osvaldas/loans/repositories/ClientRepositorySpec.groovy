package io.osvaldas.loans.repositories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.testcontainers.containers.PostgreSQLContainer

import io.osvaldas.loans.repositories.entities.Client
import io.osvaldas.loans.repositories.entities.Loan
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ClientRepositorySpec extends Specification {

    @Shared
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer('postgres:14.1-alpine')
        .withDatabaseName('loans')
        .withUsername('root')
        .withPassword('root')

    @Shared
    String NON_EXISTING_PERSON_ID = 'NON-EXISTING-ID'

    @Shared
    long NON_EXISTING_PERSONAL_CODE = 11111111110L

    @Shared
    private Client client = createClient()

    @Subject
    @Autowired
    ClientRepository clientRepository

    void 'should return user when id is correct'() {
        given:
            Client databaseResponse = clientRepository.save(client)
        when:
            Optional<Client> expected = clientRepository.findById(client.id)
        then:
            expected.filter(databaseResponse::equals)
    }

    void 'should be empty when id is incorrect'() {
        when:
            Optional<Client> expected = clientRepository.findById(NON_EXISTING_PERSON_ID)
        then:
            expected.isEmpty()
    }

    void 'should return true when user exists by personal code'() {
        given:
            clientRepository.save(client)
        when:
            boolean exists = clientRepository.existsByPersonalCode(client.personalCode)
        then:
            exists
    }

    void 'should return false when user exists by personal code'() {
        when:
            boolean exists = clientRepository.existsByPersonalCode(NON_EXISTING_PERSONAL_CODE)
        then:
            !exists
    }

    static Loan createLoan() {
        return new Loan().tap {
            id = 1
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
        }
    }

    static Client createClient() {
        return new Client().tap {
            id = 'ID-TO-TEST'
            firstName = 'Test'
            lastName = 'User'
            email = 'user@mail.com'
            phoneNumber = '+37062541365'
            personalCode = 11111111111L
            loans = Collections.singleton(createLoan())
        }
    }
}
