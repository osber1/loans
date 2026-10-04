package io.osvaldas.backoffice.domain.notifications

import java.time.Clock
import java.time.Duration

import org.springframework.context.ApplicationEventPublisher

import io.osvaldas.api.email.EmailMessage
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.repositories.NotificationOutboxRepository
import io.osvaldas.backoffice.repositories.entities.NotificationOutbox
import io.osvaldas.messages.RabbitMQMessageProducer
import io.osvaldas.messages.RabbitProperties
import io.osvaldas.messages.RabbitProperties.Exchanges
import io.osvaldas.messages.RabbitProperties.RoutingKeys
import spock.lang.Subject

class NotificationOutboxServiceSpec extends AbstractSpec {

    static final String TOKEN = 'activation-token'

    static final String EXCHANGE = 'internal.exchange'

    static final String ROUTING_KEY = 'internal.notification.routing-key'

    NotificationOutboxRepository repository = Mock()

    RabbitMQMessageProducer messageProducer = Mock()

    RabbitProperties rabbitProperties = Stub {
        exchanges >> Stub(Exchanges) {
            internal >> EXCHANGE
        }
        routingKeys >> Stub(RoutingKeys) {
            internalNotification >> ROUTING_KEY
        }
    }

    Clock clock = Clock.fixed(DATE.toInstant(), DATE.zone)

    ApplicationEventPublisher eventPublisher = Mock()

    @Subject
    NotificationOutboxService service =
        new NotificationOutboxService(repository, messageProducer, rabbitProperties, clock, eventPublisher)

    void 'should store the message and announce it when enqueuing'() {
        given:
            NotificationOutbox saved
        when:
            service.enqueue(new EmailMessage(CLIENT_ID, NAME, CLIENT_EMAIL, TOKEN))
        then:
            1 * repository.save(_ as NotificationOutbox) >> { NotificationOutbox row -> saved = row }
            1 * eventPublisher.publishEvent(new NotificationEnqueuedEvent())
        and:
            saved.clientId == CLIENT_ID
            saved.fullName == NAME
            saved.email == CLIENT_EMAIL
            saved.activationToken == TOKEN
            saved.publishedAt == null
    }

    void 'should publish pending messages in order and mark them as published'() {
        given:
            NotificationOutbox first = new NotificationOutbox('first', NAME, CLIENT_EMAIL, TOKEN)
            NotificationOutbox second = new NotificationOutbox('second', NAME, CLIENT_EMAIL, TOKEN)
        when:
            int published = service.relayPending()
        then:
            1 * repository.lockPending(NotificationOutboxService.BATCH_SIZE) >> [first, second]
        and:
            1 * messageProducer.publish(new EmailMessage('first', NAME, CLIENT_EMAIL, TOKEN), EXCHANGE, ROUTING_KEY)
        and:
            1 * messageProducer.publish(new EmailMessage('second', NAME, CLIENT_EMAIL, TOKEN), EXCHANGE, ROUTING_KEY)
        and:
            published == 2
            first.publishedAt == DATE
            second.publishedAt == DATE
    }

    void 'should keep messages pending and stop at the first failure when the broker is unavailable'() {
        given:
            NotificationOutbox first = new NotificationOutbox('first', NAME, CLIENT_EMAIL, TOKEN)
            NotificationOutbox second = new NotificationOutbox('second', NAME, CLIENT_EMAIL, TOKEN)
        when:
            int published = service.relayPending()
        then:
            1 * repository.lockPending(_) >> [first, second]
            1 * messageProducer.publish(_, _, _) >> { throw new IllegalStateException('rabbit is down') }
            0 * messageProducer.publish(_, _, _)
        and:
            published == 0
            first.publishedAt == null
            second.publishedAt == null
    }

    void 'should keep messages published before a failure'() {
        given:
            NotificationOutbox first = new NotificationOutbox('first', NAME, CLIENT_EMAIL, TOKEN)
            NotificationOutbox second = new NotificationOutbox('second', NAME, CLIENT_EMAIL, TOKEN)
        when:
            int published = service.relayPending()
        then:
            1 * repository.lockPending(_) >> [first, second]
            1 * messageProducer.publish(new EmailMessage('first', NAME, CLIENT_EMAIL, TOKEN), _, _)
            1 * messageProducer.publish(new EmailMessage('second', NAME, CLIENT_EMAIL, TOKEN), _, _) >> {
                throw new IllegalStateException('rabbit is down')
            }
        and:
            published == 1
            first.publishedAt == DATE
            second.publishedAt == null
    }

    void 'should purge messages published before the retention period'() {
        when:
            int purged = service.purgePublished(Duration.ofDays(7))
        then:
            1 * repository.deletePublishedBefore(DATE.minusDays(7)) >> 3
        and:
            purged == 3
    }

}
