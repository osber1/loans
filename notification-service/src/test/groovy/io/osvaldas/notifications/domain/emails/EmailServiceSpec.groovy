package io.osvaldas.notifications.domain.emails

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP

import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

import com.icegreen.greenmail.util.GreenMail

import io.osvaldas.notifications.infra.configuration.PropertiesConfig
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.internet.InternetAddress
import spock.lang.Shared
import spock.lang.Subject

class EmailServiceSpec extends AbstractEmailSpec {

    @Shared
    static GreenMail greenMail = new GreenMail(SMTP)

    @Shared
    String htmlContent = '<p>My first paragraph.</p>'

    @Shared
    String plainTextContent = 'My first paragraph.'

    @Shared
    EmailContent emailContent = new EmailContent(plainTextContent, htmlContent)

    PropertiesConfig config = Stub {
        senderAddress >> emailSender
        subject >> emailSubject
    }

    JavaMailSender mailSender

    @Subject
    EmailService emailService

    void setup() {
        greenMail.start()
        mailSender = new JavaMailSenderImpl()
        mailSender.port = SMTP.port
        emailService = new EmailService(mailSender, config)
    }

    void cleanup() {
        greenMail.stop()
    }

    void 'should send email'() {
        when:
            emailService.send(receiverEmail, emailContent)
        then:
            greenMail.receivedMessages.size() == 1
            with(greenMail.receivedMessages[0]) {
                from == [new InternetAddress(emailSender)]
                allRecipients.contains(new InternetAddress(receiverEmail))
                subject == emailSubject
            }
            findPart(greenMail.receivedMessages[0], 'text/plain') == plainTextContent
            findPart(greenMail.receivedMessages[0], 'text/html') == htmlContent
    }

    void 'should preserve cause when email sending fails'() {
        given:
            greenMail.stop()
        when:
            emailService.send(receiverEmail, emailContent)
        then:
            IllegalStateException e = thrown()
            e.message == 'Failed to send email.'
            e.cause instanceof MailException
    }

    private static String findPart(Part part, String mimeType) {
        if (part.isMimeType(mimeType)) {
            return part.content as String
        }
        if (part.isMimeType('multipart/*')) {
            Multipart multipart = part.content as Multipart
            return (0..<multipart.count)
                .collect { findPart(multipart.getBodyPart(it), mimeType) }
                .find { it != null }
        }
        null
    }

}
