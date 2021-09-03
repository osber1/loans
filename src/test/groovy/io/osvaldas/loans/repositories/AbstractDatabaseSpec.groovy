package io.osvaldas.loans.repositories

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.testcontainers.containers.PostgreSQLContainer

import spock.lang.Shared
import spock.lang.Specification

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
abstract class AbstractDatabaseSpec extends Specification {

    @Shared
    PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer('postgres:14.1-alpine')
        .withDatabaseName('loans')
        .withUsername('root')
        .withPassword('root')
}