package io.osvaldas.notifications.domain.emails

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP

import com.icegreen.greenmail.util.GreenMail

import spock.lang.Shared
import spock.lang.Specification

class AbstractEmailSpec extends Specification {

    @Shared
    static GreenMail greenMail = new GreenMail(SMTP)

    @Shared
    String receiverEmail = 'receiver@email.com'

    @Shared
    String emailSender = 'hello@osber.com'

    @Shared
    String emailSubject = 'Confirm your email'

}
