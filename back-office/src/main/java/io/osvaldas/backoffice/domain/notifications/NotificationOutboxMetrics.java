package io.osvaldas.backoffice.domain.notifications;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.osvaldas.backoffice.repositories.NotificationOutboxRepository;

@Component
public class NotificationOutboxMetrics {

    static final String PENDING_GAUGE = "notification.outbox.pending";

    public NotificationOutboxMetrics(MeterRegistry registry, NotificationOutboxRepository repository) {
        Gauge.builder(PENDING_GAUGE, repository, NotificationOutboxRepository::countByPublishedAtIsNull)
            .description("Notifications waiting to be relayed to the broker")
            .register(registry);
    }

}
