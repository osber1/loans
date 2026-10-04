package io.osvaldas.backoffice.infra.rest

import static java.time.Clock.fixed
import static java.time.Instant.parse
import static java.time.ZoneId.of

import java.time.Clock

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.GenericContainer
import org.testcontainers.rabbitmq.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import org.wiremock.spring.InjectWireMock

import com.github.tomakehurst.wiremock.WireMockServer

import tools.jackson.databind.ObjectMapper

import io.osvaldas.api.clients.ClientRegisterRequest
import io.osvaldas.api.loans.LoanRequest
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.SharedPostgres
import io.osvaldas.backoffice.repositories.ClientRepository
import io.osvaldas.backoffice.repositories.LoanRepository
import io.osvaldas.backoffice.repositories.NotificationOutboxRepository
import spock.lang.Shared

@Testcontainers
@ActiveProfiles('test')
@AutoConfigureMockMvc
@EnableWireMock([@ConfigureWireMock(baseUrlProperties = 'risk.checker.url')])
@ContextConfiguration(classes = [TestClockConfig, TestRabbitTopologyConfig])
@SpringBootTest(properties = 'spring.main.allow-bean-definition-overriding=true')
abstract class AbstractControllerSpec extends AbstractSpec {

    @Shared
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer('rabbitmq:4.3.6-management-alpine')

    @Shared
    @ServiceConnection
    static GenericContainer redis = new GenericContainer<>('redis:8.10.2-alpine').withExposedPorts(6379)

    @Shared
    @ServiceConnection
    static GenericContainer postgres = SharedPostgres.INSTANCE

    @InjectWireMock
    WireMockServer wireMock

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    ClientRepository clientRepository

    @Autowired
    LoanRepository loanRepository

    @Autowired
    NotificationOutboxRepository notificationOutboxRepository

    @Autowired
    CacheManager cacheManager

    static {
        redis.start()
        rabbitMQContainer.start()
    }

    void cleanup() {
        notificationOutboxRepository.deleteAll()
        loanRepository.deleteAll()
        clientRepository.deleteAll()
        cacheManager.cacheNames
            .stream()
            .each { cacheName -> cacheManager.getCache(cacheName).invalidate() }
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

    @TestConfiguration
    static class TestRabbitTopologyConfig {

        static final String EXCHANGE = 'internal.exchange'

        static final String QUEUE = 'notification.queue'

        static final String ROUTING_KEY = 'internal.notification.routing-key'

        @Bean
        DirectExchange internalExchange() {
            new DirectExchange(EXCHANGE)
        }

        @Bean
        Queue notificationQueue() {
            new Queue(QUEUE)
        }

        @Bean
        Binding notificationBinding(DirectExchange internalExchange, Queue notificationQueue) {
            BindingBuilder.bind(notificationQueue).to(internalExchange).with(ROUTING_KEY)
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

}
