package io.osvaldas.backoffice.domain.scheduler;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.osvaldas.backoffice.domain.notifications.NotificationOutboxService;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "scheduler.relayNotificationOutbox", name = "enabled", havingValue = "true")
public class NotificationOutboxScheduler {

    private final NotificationOutboxService outboxService;

    private final Duration retention;

    public NotificationOutboxScheduler(NotificationOutboxService outboxService,
                                       @Value("${scheduler.relayNotificationOutbox.retention:P7D}") Duration retention) {
        this.outboxService = outboxService;
        this.retention = retention;
    }

    @Scheduled(fixedDelayString = "${scheduler.relayNotificationOutbox.delay:PT5S}")
    @SchedulerLock(name = "relayNotificationOutbox", lockAtLeastFor = "PT1S", lockAtMostFor = "PT1M")
    public void relayNotificationOutbox() {
        int published = outboxService.relayPending();
        if (published > 0) {
            log.info("Notification outbox relayed {} messages", published);
        }
    }

    @Scheduled(cron = "${scheduler.relayNotificationOutbox.purgeCron:0 0 * * * *}")
    @SchedulerLock(name = "purgeNotificationOutbox", lockAtLeastFor = "PT1S", lockAtMostFor = "PT5M")
    public void purgeNotificationOutbox() {
        int purged = outboxService.purgePublished(retention);
        if (purged > 0) {
            log.info("Notification outbox purged {} published messages", purged);
        }
    }

}
