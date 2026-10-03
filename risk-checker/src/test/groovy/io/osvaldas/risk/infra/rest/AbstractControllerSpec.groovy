package io.osvaldas.risk.infra.rest

import static io.osvaldas.api.util.ExceptionMessages.RISK_TOO_HIGH

import java.time.Clock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.web.servlet.MockMvc
import org.wiremock.spring.EnableWireMock

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock

import io.osvaldas.risk.TestClockDelegate
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock
abstract class AbstractControllerSpec extends Specification {

    @Shared
    String riskTooHigh = RISK_TOO_HIGH.formatted('00:00', '06:00')

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    TestClockDelegate testClockDelegate

    @Value('${wiremock.server.port}')
    int wireMockPort

    WireMock getWireMock() {
        new WireMock(wireMockPort)
    }

    void cleanup() {
        testClockDelegate.reset()
        wireMock.resetRequests()
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
