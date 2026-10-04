package io.osvaldas.backoffice.domain.notifications

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.repositories.NotificationOutboxRepository

class NotificationOutboxMetricsSpec extends AbstractSpec {

    void 'should expose the number of pending notifications as a gauge'() {
        given:
            NotificationOutboxRepository repository = Stub {
                countByPublishedAtIsNull() >>> [3L, 0L]
            }
            SimpleMeterRegistry registry = new SimpleMeterRegistry()
        when:
            NotificationOutboxMetrics metrics = new NotificationOutboxMetrics(registry, repository)
        then:
            metrics != null
            registry.get(NotificationOutboxMetrics.PENDING_GAUGE).gauge().value() == 3d
            registry.get(NotificationOutboxMetrics.PENDING_GAUGE).gauge().value() == 0d
    }

}
