package io.osvaldas.risk.infra.rest

import static io.osvaldas.api.util.ExceptionMessages.RISK_TOO_HIGH

import java.time.Clock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper

import io.osvaldas.risk.TestClockDelegate
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractControllerSpec extends Specification {

    @Shared
    String riskTooHigh = RISK_TOO_HIGH.formatted('00:00', '06:00')

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

        @Bean
        ObjectMapper objectMapper() {
            new ObjectMapper()
        }

    }

}
