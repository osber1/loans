package com.finance.loans.repositories

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.testcontainers.containers.PostgreSQLContainer

import com.finance.loans.repositories.entities.Client
import com.finance.loans.repositories.entities.Loan

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

    void 'should return user when personal code is correct'() {
        given:
            Client databaseResponse = clientRepository.save(client)
        when:
            Optional<Client> expected = clientRepository.findByPersonalCode(client.personalCode)
        then:
            expected.filter(databaseResponse::equals)
    }

    void 'should be empty when personal code is incorrect'() {
        when:
            Optional<Client> expected = clientRepository.findByPersonalCode(NON_EXISTING_PERSONAL_CODE)
        then:
            expected.isEmpty()
    }

    static Loan createLoan() {
        return new Loan().with {
            id = 1
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
            return it
        } as Loan
    }

    static Client createClient() {
        return new Client().with {
            id = 'ID-TO-TEST'
            firstName = 'Test'
            lastName = 'User'
            email = 'user@mail.com'
            phoneNumber = '+37062541365'
            personalCode = 11111111111L
            loans = Collections.singleton(createLoan())
            return it
        }
    }
}
