package io.osvaldas.notifications.domain.emails

import static io.osvaldas.notifications.domain.emails.EmailBuilder.buildEmailMessage

import spock.lang.Shared
import spock.lang.Specification

class EmailBuilderSpec extends Specification {

    @Shared
    String name = 'Name'

    @Shared
    String link = 'https://www.google.com'

    void 'should contain name and link when message is generated'() {
        when:
            String message = buildEmailMessage().formatted(name, link)
        then:
            message.contains(name)
            message.contains(link)
    }

}
