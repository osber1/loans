package io.osvaldas.notifications.domain.emails

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl

import com.icegreen.greenmail.util.GreenMail

import io.osvaldas.notifications.infra.configuration.PropertiesConfig
import jakarta.mail.internet.InternetAddress
import spock.lang.Shared
import spock.lang.Subject

class EmailServiceSpec extends AbstractEmailSpec {

    @Shared
    static GreenMail greenMail = new GreenMail(SMTP)

    @Shared
    String emailContent = '<p>My first paragraph.</p>'

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
                content == "${emailContent}"
            }
    }

}
