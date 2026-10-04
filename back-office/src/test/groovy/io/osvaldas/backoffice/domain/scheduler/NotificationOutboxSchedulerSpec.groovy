package io.osvaldas.backoffice.domain.scheduler

import java.time.Duration

import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.notifications.NotificationOutboxService
import spock.lang.Subject

class NotificationOutboxSchedulerSpec extends AbstractSpec {

    static final Duration RETENTION = Duration.ofDays(7)

    NotificationOutboxService outboxService = Mock()

    @Subject
    NotificationOutboxScheduler scheduler = new NotificationOutboxScheduler(outboxService, RETENTION)

    void 'should relay pending notifications'() {
        when:
            scheduler.relayNotificationOutbox()
        then:
            1 * outboxService.relayPending() >> 2
            0 * outboxService.purgePublished(_)
    }

    void 'should purge published notifications older than the retention period'() {
        when:
            scheduler.purgeNotificationOutbox()
        then:
            1 * outboxService.purgePublished(RETENTION) >> 1
            0 * outboxService.relayPending()
    }

}
