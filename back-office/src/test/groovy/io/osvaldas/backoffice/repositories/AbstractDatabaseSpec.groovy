package io.osvaldas.backoffice.repositories

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.OPEN
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE

import java.time.ZoneId
import java.time.ZonedDateTime

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.cloud.openfeign.FeignAutoConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.spock.Testcontainers

import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = NONE)
@ImportAutoConfiguration([FeignAutoConfiguration])
abstract class AbstractDatabaseSpec extends Specification {

    @Shared
    @ServiceConnection
    static GenericContainer postgreSQLContainer = new PostgreSQLContainer('postgres:17.6-alpine')
        .withDatabaseName('loans')
        .withUsername('root')
        .withPassword('root')
        .waitingFor(Wait.forListeningPort())

    static final ZonedDateTime DATE = generateDate(2022)

    static final ZonedDateTime FUTURE_DATE = generateDate(2111)

    static final String INVALID_CLIENT_ID = 'invalidClientId'

    static final String VALID_CLIENT_ID = 'validClientId'

    static final String VALID_PERSONAL_CODE = '12345678911'

    static final String INVALID_PERSONAL_CODE = '12345678910'

    static final int INVALID_LOAN_ID = 4758758

    Client client = createClient()

    Loan loan = createLoan()

    static {
        postgreSQLContainer.start()
    }

    static ZonedDateTime generateDate(int year) {
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

    private static Loan createLoan() {
        new Loan().tap {
            amount = 10.00
            interestRate = 10.00
            termInMonths = 10
            createdAt = DATE
            status = OPEN
        }
    }

    private static Client createClient() {
        new Client().tap {
            id = VALID_CLIENT_ID
            firstName = 'Test'
            lastName = 'User'
            email = 'user@mail.com'
            phoneNumber = '+37062541365'
            personalCode = VALID_PERSONAL_CODE
            status = ACTIVE
        }
    }

}
