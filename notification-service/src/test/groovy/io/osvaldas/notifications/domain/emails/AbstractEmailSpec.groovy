package io.osvaldas.notifications.domain.emails

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP

import org.testcontainers.containers.GenericContainer

import com.icegreen.greenmail.util.GreenMail

import spock.lang.Shared
import spock.lang.Specification

class AbstractEmailSpec extends Specification {

    @Shared
    static GreenMail greenMail = new GreenMail(SMTP)

    @Shared
    static GenericContainer mailhogContainer = new GenericContainer<>('mailhog/mailhog:v1.0.1')
        .withExposedPorts(1025, 8025)

    @Shared
    String receiverEmail = 'receiver@email.com'

    @Shared
    String emailSender = 'hello@osber.com'

    @Shared
    String emailSubject = 'Confirm your email'

}
