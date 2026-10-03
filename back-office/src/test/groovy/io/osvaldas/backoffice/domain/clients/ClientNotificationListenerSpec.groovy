package io.osvaldas.backoffice.domain.clients

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT

import org.springframework.transaction.event.TransactionalEventListener

import io.osvaldas.api.email.EmailMessage
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.messages.RabbitMQMessageProducer
import io.osvaldas.messages.RabbitProperties
import io.osvaldas.messages.RabbitProperties.Exchanges
import io.osvaldas.messages.RabbitProperties.RoutingKeys
import spock.lang.Subject

class ClientNotificationListenerSpec extends AbstractSpec {

    RabbitMQMessageProducer messageProducer = Mock()

    RabbitProperties rabbitProperties = Stub {
        exchanges >> Stub(Exchanges) {
            internal >> 'internal.exchange'
        }
        routingKeys >> Stub(RoutingKeys) {
            internalNotification >> 'internal.notification.routing-key'
        }
    }

    @Subject
    ClientNotificationListener listener = new ClientNotificationListener(messageProducer, rabbitProperties)

    void 'should publish registration email message when client registered event is received'() {
        when:
            listener.onClientRegistered(new ClientRegisteredEvent(CLIENT_ID, NAME + ' ' + SURNAME, CLIENT_EMAIL))
        then:
            1 * messageProducer.publish(
                new EmailMessage(CLIENT_ID, NAME + ' ' + SURNAME, CLIENT_EMAIL),
                'internal.exchange',
                'internal.notification.routing-key')
    }

    void 'should publish registration email only after transaction commit'() {
        when:
            TransactionalEventListener annotation = ClientNotificationListener
                .getMethod('onClientRegistered', ClientRegisteredEvent)
                .getAnnotation(TransactionalEventListener)
        then:
            annotation.phase() == AFTER_COMMIT
            !annotation.fallbackExecution()
    }

}
