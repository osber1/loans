package io.osvaldas.fraud.infra.rest

import java.time.Clock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper

import io.osvaldas.fraud.TestClockDelegate
import spock.lang.Specification

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8080)
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

    @Autowired
    TestClockDelegate testClockDelegate

    void cleanup() {
        testClockDelegate.reset()
    }

    @TestConfiguration
    static class TestClockConfig {

        @Primary
        @Bean
        TestClockDelegate testClockDelegate(Clock clock) {
            new TestClockDelegate(clock)
        }

    }

}
