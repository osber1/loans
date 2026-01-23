package io.osvaldas.notifications

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
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
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer('rabbitmq:3.13.1-management-alpine')

    @Shared
    static GenericContainer mailhogContainer = new GenericContainer<>('mailhog/mailhog:v1.0.1')
        .withExposedPorts(1025, 8025)

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
    static void rabbitMqProperties(DynamicPropertyRegistry registry) {
        registry.add('spring.mail.host', mailhogContainer::getHost)
        registry.add('spring.mail.port') { mailhogContainer.getMappedPort(1025) }
    }

    void cleanupSpec() {
        rabbitMQContainer.stop()
        mailhogContainer.stop()
    }

}
