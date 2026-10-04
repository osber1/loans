package io.osvaldas.notifications

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.rabbitmq.RabbitMQContainer

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

    static String deadLetterQueueName = 'notification.dlq'

    @ServiceConnection
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer('rabbitmq:4.3.6-management-alpine')

    static GenericContainer mailhogContainer = new GenericContainer<>('mailhog/mailhog:v1.0.1')
        .withExposedPorts(MAILHOG_SMTP_PORT, MAILHOG_API_PORT)

    static {
        mailhogContainer.start()
        rabbitMQContainer.start()

        rabbitMQContainer.with {
            execInContainer('rabbitmqadmin', 'declare', 'queue', '--name', queueName)
            execInContainer('rabbitmqadmin', 'declare', 'exchange', '--name', exchangeName, '--type', 'direct')
            execInContainer('rabbitmqadmin', 'declare', 'binding',
                '--source', exchangeName,
                '--destination', queueName,
                '--destination-type', 'queue',
                '--routing-key', routingKeys)
            // Mirrors the dead-letter queue and notification-dlx policy from loans-infra
            execInContainer('rabbitmqadmin', 'declare', 'queue', '--name', deadLetterQueueName)
            execInContainer('rabbitmqctl', 'set_policy', 'notification-dlx', '^notification\\.queue$',
                '{"dead-letter-exchange":"","dead-letter-routing-key":"' + deadLetterQueueName + '"}',
                '--apply-to', 'queues')
        }
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
