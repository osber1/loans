package io.osvaldas.backoffice.infra.rest

import static java.time.Clock.fixed
import static java.time.Instant.parse
import static java.time.ZoneId.of

import java.time.Clock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.RabbitMQContainer

import com.fasterxml.jackson.databind.ObjectMapper

import io.osvaldas.api.clients.ClientRegisterRequest
import io.osvaldas.api.loans.LoanRequest
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.repositories.ClientRepository
import io.osvaldas.backoffice.repositories.LoanRepository
import spock.lang.Shared

@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractControllerSpec extends AbstractSpec {

    @Shared
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer('rabbitmq:3.9.11-management-alpine')

    @Value('${exceptionMessages.clientErrorMessage:}')
    String clientErrorMessage

    @Value('${exceptionMessages.loanErrorMessage:}')
    String loanErrorMessage

    @Value('${exceptionMessages.riskMessage:}')
    String riskMessage

    @Value('${exceptionMessages.amountExceedsMessage:}')
    String amountExceedsMessage

    @Value('${exceptionMessages.loanLimitExceedsMessage:}')
    String loanLimitExceedsMessage

    @Value('${exceptionMessages.clientNotActiveMessage:}')
    String clientNotActiveMessage

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    ClientRepository clientRepository

    @Autowired
    LoanRepository loanRepository

    @DynamicPropertySource
    static void rabbitMqProperties(DynamicPropertyRegistry registry) {
        rabbitMQContainer.start()
        registry.add('spring.rabbitmq.host') { rabbitMQContainer.host }
        registry.add('spring.rabbitmq.port') { rabbitMQContainer.getMappedPort(5672) }
    }

    void cleanup() {
        clientRepository.deleteAll()
        loanRepository.deleteAll()
        rabbitMQContainer.stop()
    }

    ClientRegisterRequest buildClientRequest(String clientName,
                                             String clientSurname,
                                             String clientCode,
                                             String clientEmail,
                                             String clientPhoneNumber) {
        new ClientRegisterRequest().tap {
            firstName = clientName
            lastName = clientSurname
            personalCode = clientCode
            email = clientEmail
            phoneNumber = clientPhoneNumber
        }
    }

    LoanRequest buildLoanRequest(BigDecimal loanAmount) {
        new LoanRequest().tap {
            amount = loanAmount
            termInMonths = 12
        }
    }

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        Clock clock() {
            fixed(parse('2021-10-12T10:10:10.00Z'), of('UTC'))
        }

    }

}
