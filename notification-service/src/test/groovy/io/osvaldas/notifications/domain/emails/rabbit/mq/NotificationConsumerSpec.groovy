package io.osvaldas.notifications.domain.emails.rabbit.mq

import io.osvaldas.api.email.EmailMessage
import io.osvaldas.notifications.domain.emails.EmailSender
import io.osvaldas.notifications.infra.configuration.PropertiesConfig
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class NotificationConsumerSpec extends Specification {

    @Shared
    String clientId = 'clientId'

    @Shared
    String link = 'http://localhost:8080/api/v1/client/%s/active'

    @Shared
    String fullName = 'Name Surname'

    @Shared
    String email = 'user@email.com'

    @Shared
    EmailMessage emailMessage = new EmailMessage(clientId, fullName, email)

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
            1 * emailSender.send(email) { String data ->
                with(data) {
                    contains(fullName)
                    contains(link.formatted(clientId))
                }
            }
    }

}
