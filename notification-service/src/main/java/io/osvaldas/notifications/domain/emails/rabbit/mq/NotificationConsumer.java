package io.osvaldas.notifications.domain.emails.rabbit.mq;

import static io.osvaldas.notifications.domain.emails.EmailBuilder.buildEmailMessage;

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
        log.info("Received message: {}", message);
        String activationLink = config.getActivationLink().formatted(message.clientId());
        String emailContent = buildEmailMessage().formatted(message.fullName(), activationLink);
        log.info("Sending email to: {}", message.email());
        emailSender.send(message.email(), emailContent);
    }
}
