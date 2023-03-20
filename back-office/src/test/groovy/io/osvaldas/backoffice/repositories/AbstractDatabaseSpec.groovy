package io.osvaldas.backoffice.repositories

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.OPEN
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE

import java.time.ZoneId
import java.time.ZonedDateTime

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.testcontainers.containers.PostgreSQLContainer

import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
abstract class AbstractDatabaseSpec extends Specification {

    @Shared
    ZonedDateTime date = generateDate(2022)

    @Shared
    ZonedDateTime futureDate = generateDate(2111)

    @Shared
    String invalidClientId = 'invalidClientId'

    @Shared
    String validClientId = 'validClientId'

    @Shared
    String validPersonalCode = '12345678911'

    @Shared
    String invalidPersonalCode = '12345678910'

    @Shared
    Client client = createClient()

    @Shared
    int invalidLoanId = 4758758

    @Shared
    Loan loan = createLoan()

    @Shared
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer('postgres:15.2-alpine')
        .withDatabaseName('loans')
        .withUsername('root')
        .withPassword('root')

    ZonedDateTime generateDate(int year) {
        ZonedDateTime.of(
            year,
            1,
            5,
            10,
            0,
            0,
            0,
            ZoneId.of('UTC'))
    }

    private Loan createLoan() {
        new Loan().tap {
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
            createdAt = date
            status = OPEN
        }
    }

    private Client createClient() {
        new Client().tap {
            id = validClientId
            firstName = 'Test'
            lastName = 'User'
            email = 'user@mail.com'
            phoneNumber = '+37062541365'
            personalCode = validPersonalCode
            status = ACTIVE
        }
    }

}
