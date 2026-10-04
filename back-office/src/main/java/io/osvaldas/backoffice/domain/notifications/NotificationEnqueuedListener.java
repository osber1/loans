package io.osvaldas.backoffice.domain.notifications;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEnqueuedListener {

    private final NotificationOutboxService outboxService;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onNotificationEnqueued(NotificationEnqueuedEvent event) {
        try {
            outboxService.relayPending();
        } catch (RuntimeException e) {
            log.warn("Immediate notification relay failed, the scheduler will retry", e);
        }
    }

}
