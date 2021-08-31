package com.finance.interest.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.testcontainers.containers.PostgreSQLContainer

import com.finance.interest.model.ClientDAO
import com.finance.interest.model.Loan

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ClientRepositoryTest extends Specification {

    @Shared
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:latest")
        .withDatabaseName("loans")
        .withUsername("root")
        .withPassword("root")

    @Shared
    String NON_EXISTING_PERSON_ID = 'NON-EXISTING-ID'

    @Shared
    long NON_EXISTING_PERSONAL_CODE = 11111111110L

    @Shared
    private ClientDAO client = createClient()

    @Subject
    @Autowired
    private ClientRepository clientRepository

    void 'should return user when id is correct'() {
        given:
            ClientDAO databaseResponse = clientRepository.save(client)
        when:
            Optional<ClientDAO> expected = clientRepository.findById(client.id)
        then:
            expected.filter(databaseResponse::equals)
    }

    void 'should be empty when id is incorrect'() {
        when:
            Optional<ClientDAO> expected = clientRepository.findById(NON_EXISTING_PERSON_ID)
        then:
            expected.isEmpty()
    }

    void 'should return user when personal code is correct'() {
        given:
            ClientDAO databaseResponse = clientRepository.save(client)
        when:
            Optional<ClientDAO> expected = clientRepository.findByPersonalCode(client.personalCode)
        then:
            expected.filter(databaseResponse::equals)
    }

    void 'should be empty when personal code is incorrect'() {
        when:
            Optional<ClientDAO> expected = clientRepository.findByPersonalCode(NON_EXISTING_PERSONAL_CODE)
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

    static ClientDAO createClient() {
        return new ClientDAO().with {
            id = 'ID-TO-TEST'
            firstName = 'Test'
            lastName = 'Uset'
            personalCode = 11111111111L
            loans = Collections.singleton(createLoan())
            return it
        }
    }
}
