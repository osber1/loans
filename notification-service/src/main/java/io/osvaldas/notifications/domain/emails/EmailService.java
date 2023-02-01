package io.osvaldas.notifications.domain.emails;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.osvaldas.notifications.infra.configuration.PropertiesConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailService implements EmailSender {

    private final JavaMailSender mailSender;

    private final PropertiesConfig config;

    @Async
    @Override
    public void send(String receiverEmail, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(content, true);
            helper.setTo(receiverEmail);
            helper.setSubject(config.getSubject());
            helper.setFrom(config.getSenderAddress());
            mailSender.send(mimeMessage);
            log.info("Email sent to: {}", receiverEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to " + receiverEmail, e);
            throw new IllegalStateException("Failed to send email.");
        }
    }
}
