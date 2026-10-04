package io.osvaldas.backoffice.domain.notifications

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT

import org.springframework.transaction.event.TransactionalEventListener

import io.osvaldas.backoffice.AbstractSpec
import spock.lang.Subject

class NotificationEnqueuedListenerSpec extends AbstractSpec {

    NotificationOutboxService outboxService = Mock()

    @Subject
    NotificationEnqueuedListener listener = new NotificationEnqueuedListener(outboxService)

    void 'should relay pending notifications when a notification is enqueued'() {
        when:
            listener.onNotificationEnqueued(new NotificationEnqueuedEvent())
        then:
            1 * outboxService.relayPending()
    }

    void 'should not fail the request when the immediate relay fails'() {
        when:
            listener.onNotificationEnqueued(new NotificationEnqueuedEvent())
        then:
            1 * outboxService.relayPending() >> { throw new IllegalStateException('rabbit is down') }
        and:
            noExceptionThrown()
    }

    void 'should relay only after the transaction has committed'() {
        when:
            TransactionalEventListener annotation = NotificationEnqueuedListener
                .getMethod('onNotificationEnqueued', NotificationEnqueuedEvent)
                .getAnnotation(TransactionalEventListener)
        then:
            annotation.phase() == AFTER_COMMIT
            !annotation.fallbackExecution()
    }

}
