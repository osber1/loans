package io.osvaldas.notifications.domain.emails

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP

import com.icegreen.greenmail.util.GreenMail

import spock.lang.Shared
import spock.lang.Specification

class AbstractEmailSpec extends Specification {

    @Shared
    String receiverEmail = 'receiver@email.com'

    @Shared
    String emailSender = 'hello@osber.com'

    @Shared
    String emailSubject = 'Confirm your email'

    @Shared
    static GreenMail greenMail = new GreenMail(SMTP)
}