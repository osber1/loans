package io.osvaldas.notifications.domain.emails.rabbit.mq;

import static io.osvaldas.notifications.domain.emails.EmailBuilder.buildActivationEmail;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import io.osvaldas.api.email.EmailMessage;
import io.osvaldas.notifications.domain.emails.EmailSender;
import io.osvaldas.notifications.infra.configuration.PropertiesConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class NotificationConsumer {

    private final EmailSender emailSender;

    private final PropertiesConfig config;

    @RabbitListener(queues = "${rabbitmq.queues.notification}")
    public void consume(EmailMessage message) {
        log.info("Received activation email request for client: {}", message.clientId());
        String activationLink = config.getActivationLink().formatted(message.clientId());
        emailSender.send(message.email(), buildActivationEmail(message.fullName(), activationLink));
        log.info("Activation email sent for client: {}", message.clientId());
    }
}
