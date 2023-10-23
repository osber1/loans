package io.osvaldas.notifications.domain.emails

import spock.lang.Shared
import spock.lang.Specification

class AbstractEmailSpec extends Specification {

    @Shared
    String receiverEmail = 'receiver@email.com'

    @Shared
    String emailSender = 'hello@osber.com'

    @Shared
    String emailSubject = 'Confirm your email'

}
