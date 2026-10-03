package io.osvaldas.notifications

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.RabbitMQContainer

import io.osvaldas.notifications.domain.emails.AbstractEmailSpec

/**
 * Containers are started once per JVM (singleton container pattern) and are shared by all
 * integration specs and the cached Spring context. They are cleaned up by Testcontainers (Ryuk)
 * when the JVM exits, so they must not be stopped after an individual spec.
 */
@SpringBootTest
abstract class AbstractIntegrationSpec extends AbstractEmailSpec {

    static final int MAILHOG_SMTP_PORT = 1025

    static final int MAILHOG_API_PORT = 8025

    static String exchangeName = 'internal.exchange'

    static String queueName = 'notification.queue'

    static String routingKeys = 'internal.notification.routing-key'

    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer('rabbitmq:3.13.1-management-alpine')

    static GenericContainer mailhogContainer = new GenericContainer<>('mailhog/mailhog:v1.0.1')
        .withExposedPorts(MAILHOG_SMTP_PORT, MAILHOG_API_PORT)

    static {
        mailhogContainer.start()
        rabbitMQContainer.start()

        rabbitMQContainer.execInContainer('rabbitmqadmin', 'declare', 'queue', "name=$queueName")
        rabbitMQContainer.execInContainer('rabbitmqadmin', 'declare', 'exchange', "name=$exchangeName", 'type=direct')
        rabbitMQContainer.execInContainer('rabbitmqadmin', 'declare', 'binding',
            "source=$exchangeName",
            "destination=$queueName",
            "routing_key=$routingKeys",
            'destination_type=queue',
            'arguments={}')
    }

    @DynamicPropertySource
    static void mailProperties(DynamicPropertyRegistry registry) {
        registry.add('spring.mail.host', mailhogContainer::getHost)
        registry.add('spring.mail.port') { mailhogContainer.getMappedPort(MAILHOG_SMTP_PORT) }
    }

    static String mailhogApiUrl(String path) {
        "http://${mailhogContainer.host}:${mailhogContainer.getMappedPort(MAILHOG_API_PORT)}${path}"
    }

}
