package io.osvaldas.fraud.infra.rest

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
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Specification

@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractControllerSpec extends Specification {

    @Value('${exceptionMessages.riskMessage:}')
    String riskMessage

    @Value('${exceptionMessages.amountExceedsMessage:}')
    String amountExceedsMessage

    @Value('${exceptionMessages.loanLimitExceedsMessage:}')
    String loanLimitExceedsMessage

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        Clock clock() {
            fixed(parse('2022-10-12T10:10:10.00Z'), of('UTC'))
        }

    }

}
