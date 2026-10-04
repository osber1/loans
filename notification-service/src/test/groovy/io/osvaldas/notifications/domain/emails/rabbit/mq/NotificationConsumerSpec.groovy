package io.osvaldas.notifications.domain.emails.rabbit.mq

import io.osvaldas.api.email.EmailMessage
import io.osvaldas.notifications.domain.emails.EmailContent
import io.osvaldas.notifications.domain.emails.EmailSender
import io.osvaldas.notifications.infra.configuration.PropertiesConfig
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class NotificationConsumerSpec extends Specification {

    @Shared
    String clientId = 'clientId'

    @Shared
    String link = 'http://localhost:8080/api/v1/clients/%s/activation?token=%s'

    @Shared
    String token = 'activation-token'

    @Shared
    String fullName = 'Name Surname'

    @Shared
    String email = 'user@email.com'

    @Shared
    EmailMessage emailMessage = new EmailMessage(clientId, fullName, email, token)

    EmailSender emailSender = Mock()

    PropertiesConfig config = Stub {
        activationLink >> link
    }

    @Subject
    NotificationConsumer consumer = new NotificationConsumer(emailSender, config)

    void 'should consume message and send email'() {
        when:
            consumer.consume(emailMessage)
        then:
            1 * emailSender.send(email, _ as EmailContent) >> { String receiver, EmailContent content ->
                assert content.html().contains(fullName)
                assert content.html().contains(link.formatted(clientId, token))
                assert content.plainText().contains(fullName)
                assert content.plainText().contains(link.formatted(clientId, token))
            }
    }

}
