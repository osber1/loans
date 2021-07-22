package com.finance.interest.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

import com.finance.interest.model.ClientDAO
import com.finance.interest.model.Loan

import spock.lang.Specification

@DataJpaTest
class ClientRepositoryTest extends Specification {

    public static final String NON_EXISTING_PERSON_ID = 'NON-EXISTING-ID'

    private static final long NON_EXISTING_PERSONAL_CODE = 11111111110L

    private ClientDAO client = createClient()

    @Autowired
    private ClientRepository underTest

    void 'should return user when id is correct'() {
        given:
            ClientDAO databaseResponse = underTest.save(client)
        when:
            Optional<ClientDAO> expected = underTest.findById(client.id)
        then:
            expected.filter(databaseResponse::equals)
    }

    void 'should be empty when id is incorrect'() {
        when:
            Optional<ClientDAO> expected = underTest.findById(NON_EXISTING_PERSON_ID)
        then:
            expected.isEmpty()
    }

    void 'should return user when personal code is correct'() {
        given:
            ClientDAO databaseResponse = underTest.save(client)
        when:
            Optional<ClientDAO> expected = underTest.findByPersonalCode(client.personalCode)
        then:
            expected.filter(databaseResponse::equals)
    }

    void 'should be empty when personal code is incorrect'() {
        when:
            Optional<ClientDAO> expected = underTest.findByPersonalCode(NON_EXISTING_PERSONAL_CODE)
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
