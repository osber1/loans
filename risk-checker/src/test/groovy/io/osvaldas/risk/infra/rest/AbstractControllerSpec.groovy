package io.osvaldas.risk.infra.rest

import java.time.Clock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper

import io.osvaldas.risk.TestClockDelegate
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8080)
abstract class AbstractControllerSpec extends Specification {

    @Shared
    String riskTooHigh = 'Risk is too high, because you are trying ' +
        'to get loan between 00:00 and 6:00 and you want to borrow the max amount!'

    @Shared
    String amountExceeds = 'The amount you are trying to borrow exceeds the max amount!'

    @Shared
    String loanLimitExceeds = 'Too many loans taken in a single day.'

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

        @Bean
        @Primary
        TestClockDelegate testClockDelegate(Clock clock) {
            new TestClockDelegate(clock)
        }

    }

}
