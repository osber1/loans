package io.osvaldas.backoffice

import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.postgresql.PostgreSQLContainer

final class SharedPostgres {

    static final String CREDENTIAL = 'root'

    static final PostgreSQLContainer INSTANCE = new PostgreSQLContainer('postgres:18.6-alpine')
        .withDatabaseName('loans')
        .withUsername(CREDENTIAL)
        .withPassword(CREDENTIAL)
        .waitingFor(Wait.forListeningPort())

    static {
        INSTANCE.start()
    }

    private SharedPostgres() {
    }

}
