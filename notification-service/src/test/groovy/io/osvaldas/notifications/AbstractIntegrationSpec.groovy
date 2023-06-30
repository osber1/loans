package io.osvaldas.notifications

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP
import static java.util.Collections.emptyMap

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.RabbitMQContainer

import io.osvaldas.notifications.domain.emails.AbstractEmailSpec
import spock.lang.Shared

@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationSpec extends AbstractEmailSpec {

    static String exchangeName = 'internal.exchange'

    static String queueName = 'notification.queue'

    static String routingKeys = 'internal.notification.routing-key'

    @Shared
    static RabbitMQContainer rabbitMQContainer = new RabbitMQContainer('rabbitmq:3.9.11-management-alpine')
        .withQueue(queueName)
        .withExchange(exchangeName, 'direct')
        .withBinding(exchangeName, queueName, emptyMap(), routingKeys, 'queue')

    static {
        mailhogContainer.start()
        rabbitMQContainer.start()
    }

    @DynamicPropertySource
    static void rabbitMqProperties(DynamicPropertyRegistry registry) {
        registry.add('spring.rabbitmq.host') { rabbitMQContainer.host }
        registry.add('spring.rabbitmq.port') { rabbitMQContainer.getMappedPort(5672) }
        registry.add('email.port') { SMTP.port }

        Integer mailHogSMTPPort = mailhogContainer.getMappedPort(1025)
        registry.add('spring.mail.host', mailhogContainer::getHost)
        registry.add('spring.mail.port', mailHogSMTPPort::toString)
    }

    void cleanupSpec() {
        rabbitMQContainer.stop()
        mailhogContainer.stop()
    }

}
