package io.osvaldas.backoffice.infra.rest

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS
import static java.time.Clock.fixed
import static java.time.Instant.parse
import static java.time.ZoneId.of

import java.time.Clock

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

import io.osvaldas.api.clients.ClientRegisterRequest
import io.osvaldas.api.loans.LoanRequest
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.repositories.ClientRepository
import io.osvaldas.backoffice.repositories.LoanRepository
import spock.lang.Shared

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ContextConfiguration(classes = TestObjectMapperConfig)
abstract class AbstractControllerSpec extends AbstractSpec {

    @Shared
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer('rabbitmq:3.13.1-management-alpine')

    @Shared
    @ServiceConnection
    static GenericContainer redis = new GenericContainer<>('redis:7.2.4-alpine').withExposedPorts(6379)

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    ClientRepository clientRepository

    @Autowired
    LoanRepository loanRepository

    @Autowired
    CacheManager cacheManager

    static {
        redis.start()
        rabbitMQContainer.start()
    }

    void cleanup() {
        clientRepository.deleteAll()
        loanRepository.deleteAll()
        cacheManager.cacheNames
            .stream()
            .each { cacheName -> cacheManager.getCache(cacheName).clear() }
    }

    ClientRegisterRequest buildClientRequest(String clientName,
                                             String clientSurname,
                                             String clientCode,
                                             String clientEmail,
                                             String clientPhoneNumber) {
        new ClientRegisterRequest(clientName, clientSurname, clientEmail, clientPhoneNumber, clientCode)
    }

    LoanRequest buildLoanRequest(BigDecimal loanAmount) {
        new LoanRequest(loanAmount, 12)
    }

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        Clock clock() {
            fixed(parse('2021-10-12T10:10:10.00Z'), of('UTC'))
        }

    }

    @EnableCaching
    @TestConfiguration
    static class TestCacheConfig {

        @Bean
        CacheManager cacheManager() {
            new ConcurrentMapCacheManager('LoanResponse')
        }
    }

    @TestConfiguration
    static class TestObjectMapperConfig {

        @Bean
        ObjectMapper objectMapper() {
            JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(WRITE_DATE_KEYS_AS_TIMESTAMPS)
                .build()
        }

    }

}
