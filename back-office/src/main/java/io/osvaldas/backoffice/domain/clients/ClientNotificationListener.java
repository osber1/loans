package io.osvaldas.backoffice.domain.clients;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import io.osvaldas.api.email.EmailMessage;
import io.osvaldas.messages.RabbitMQMessageProducer;
import io.osvaldas.messages.RabbitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientNotificationListener {

    private final RabbitMQMessageProducer messageProducer;

    private final RabbitProperties rabbitProperties;

    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onClientRegistered(ClientRegisteredEvent event) {
        log.info("Sending registration notification for client: {}", event.clientId());
        EmailMessage message = new EmailMessage(event.clientId(), event.fullName(), event.email());
        messageProducer.publish(message, rabbitProperties.getExchanges().getInternal(), rabbitProperties.getRoutingKeys().getInternalNotification());
    }

}
