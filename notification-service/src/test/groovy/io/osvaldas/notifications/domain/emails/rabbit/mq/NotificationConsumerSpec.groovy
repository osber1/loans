package io.osvaldas.notifications.domain.emails.rabbit.mq

import io.osvaldas.api.EmailMessage
import io.osvaldas.notifications.domain.emails.EmailSender
import io.osvaldas.notifications.infra.configuration.PropertiesConfig
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class NotificationConsumerSpec extends Specification {

    @Shared
    String email = "email"

    @Shared
    EmailMessage emailMessage = new EmailMessage(email: email)

    EmailSender emailSender = Mock()

    PropertiesConfig config = Stub {
        activationLink >> 'http://localhost:8080/api/v1/client/%s/active'
    }

    @Subject
    NotificationConsumer consumer = new NotificationConsumer(emailSender, config)

    void 'should consume message and send email'() {
        when:
            consumer.consume(emailMessage)
        then:
            1 * emailSender.send(email, _ as String)
    }
}
