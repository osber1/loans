package io.osvaldas.notifications.domain.emails;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import io.osvaldas.notifications.infra.configuration.PropertiesConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailService implements EmailSender {

    private final JavaMailSender mailSender;

    private final PropertiesConfig config;

    @Override
    public void send(String receiverEmail, EmailContent content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF_8.name());
            helper.setText(content.plainText(), content.html());
            helper.setTo(receiverEmail);
            helper.setSubject(config.getSubject());
            helper.setFrom(config.getSenderAddress());
            mailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            throw new IllegalStateException("Failed to send email.", e);
        }
    }
}
