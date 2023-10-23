package io.osvaldas.notifications

import static java.util.Collections.emptyMap

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers

import io.osvaldas.notifications.domain.emails.AbstractEmailSpec
import spock.lang.Shared

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationSpec extends AbstractEmailSpec {

    static String exchangeName = 'internal.exchange'

    static String queueName = 'notification.queue'

    static String routingKeys = 'internal.notification.routing-key'

    @Shared
    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer('rabbitmq:3.12.2-management-alpine')
        .withQueue(queueName)
        .withExchange(exchangeName, 'direct')
        .withBinding(exchangeName, queueName, emptyMap(), routingKeys, 'queue')

    @Shared
    static GenericContainer mailhogContainer = new GenericContainer<>('mailhog/mailhog:v1.0.1')
        .withExposedPorts(1025, 8025)

    static {
        mailhogContainer.start()
        rabbitMQContainer.start()
    }

    @DynamicPropertySource
    static void rabbitMqProperties(DynamicPropertyRegistry registry) {
        registry.add('spring.mail.host', mailhogContainer::getHost)
        registry.add('spring.mail.port') { mailhogContainer.getMappedPort(1025) }
    }

    void cleanupSpec() {
        rabbitMQContainer.stop()
        mailhogContainer.stop()
    }

}
