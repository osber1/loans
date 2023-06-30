package io.osvaldas.notifications

import static org.testcontainers.shaded.org.awaitility.Awaitility.await

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.api.email.EmailMessage
import spock.lang.Shared

class IntegrationSpec extends AbstractIntegrationSpec {

    @Shared
    String fullName = 'Name Surname'

    @Autowired
    AmqpTemplate amqpTemplate

    void 'should send email when message is consumed'() {
        given:
            EmailMessage message = new EmailMessage('clientId', fullName, receiverEmail)
        when:
            amqpTemplate.convertAndSend(exchangeName, routingKeys, message)
        then:
            await().until {
                mailhogContainer.logs.contains(emailSender)
                mailhogContainer.logs.contains(receiverEmail)
                mailhogContainer.logs.contains(emailSubject)
                mailhogContainer.logs.contains(fullName)
            }
    }

}
