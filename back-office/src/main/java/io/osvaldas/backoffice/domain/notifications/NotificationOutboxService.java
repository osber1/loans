package io.osvaldas.backoffice.domain.notifications;

import static org.springframework.transaction.annotation.Propagation.MANDATORY;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.time.Duration;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.api.email.EmailMessage;
import io.osvaldas.api.util.TimeUtils;
import io.osvaldas.backoffice.repositories.NotificationOutboxRepository;
import io.osvaldas.backoffice.repositories.entities.NotificationOutbox;
import io.osvaldas.messages.RabbitMQMessageProducer;
import io.osvaldas.messages.RabbitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationOutboxService {

    static final int BATCH_SIZE = 50;

    private final NotificationOutboxRepository repository;

    private final RabbitMQMessageProducer messageProducer;

    private final RabbitProperties rabbitProperties;

    private final TimeUtils timeUtils;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = MANDATORY)
    public void enqueue(EmailMessage message) {
        repository.save(new NotificationOutbox(message.clientId(), message.fullName(), message.email(), message.activationToken()));
        eventPublisher.publishEvent(new NotificationEnqueuedEvent());
    }

    @Transactional(propagation = REQUIRES_NEW)
    public int relayPending() {
        List<NotificationOutbox> pending = repository.lockPending(BATCH_SIZE);
        int published = 0;
        for (NotificationOutbox row : pending) {
            if (!publish(row)) {
                break;
            }
            row.setPublishedAt(timeUtils.getCurrentDateTime());
            published++;
        }
        return published;
    }

    @Transactional
    public int purgePublished(Duration retention) {
        return repository.deletePublishedBefore(timeUtils.getCurrentDateTime().minus(retention));
    }

    private boolean publish(NotificationOutbox row) {
        try {
            messageProducer.publish(
                new EmailMessage(row.getClientId(), row.getFullName(), row.getEmail(), row.getActivationToken()),
                rabbitProperties.getExchanges().getInternal(),
                rabbitProperties.getRoutingKeys().getInternalNotification());
            return true;
        } catch (RuntimeException e) {
            log.warn("Failed to publish notification {} for client {}, it will be retried", row.getId(), row.getClientId(), e);
            return false;
        }
    }

}
