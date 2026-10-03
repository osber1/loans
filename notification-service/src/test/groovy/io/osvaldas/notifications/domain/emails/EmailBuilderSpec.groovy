package io.osvaldas.notifications.domain.emails

import static io.osvaldas.notifications.domain.emails.EmailBuilder.buildActivationEmail

import spock.lang.Shared
import spock.lang.Specification

class EmailBuilderSpec extends Specification {

    @Shared
    String name = 'Name'

    @Shared
    String link = 'http://localhost:8080/api/v1/clients/clientId/active'

    void 'should contain name and link when message is generated'() {
        when:
            EmailContent content = buildActivationEmail(name, link)
        then:
            content.html().contains("Hi ${name},")
            content.html().contains("<a href=\"${link}\">Activate Now</a>")
            content.plainText().contains("Hi ${name},")
            content.plainText().contains(link)
    }

    void 'should not leave template placeholders or stray tags in html'() {
        when:
            String html = buildActivationEmail(name, link).html()
        then:
            !html.contains('{{')
            !html.contains('}}')
            html.count('<a ') == html.count('</a>')
    }

    void 'should escape html in full name'() {
        given:
            String maliciousName = '<script>alert("x")</script><a href=\'https://evil.com\'>Click</a> & Co'
        when:
            EmailContent content = buildActivationEmail(maliciousName, link)
        then:
            !content.html().contains('<script>')
            !content.html().contains('evil.com\'>')
            content.html().contains('&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;')
            content.html().contains('&#39;https://evil.com&#39;&gt;Click&lt;/a&gt; &amp; Co')
            content.plainText().contains("Hi ${maliciousName},")
    }

    void 'should escape html in activation link'() {
        given:
            String linkWithQuotes = 'http://localhost/a?b=1&c="><script>'
        when:
            String html = buildActivationEmail(name, linkWithQuotes).html()
        then:
            html.contains('<a href="http://localhost/a?b=1&amp;c=&quot;&gt;&lt;script&gt;">')
    }

    void 'should not interpret placeholders or replacement tokens in user input'() {
        given:
            String trickyName = '{{activationLink}} $1 \\ %s'
        when:
            EmailContent content = buildActivationEmail(trickyName, link)
        then:
            content.plainText().contains("Hi ${trickyName},")
            content.html().contains("Hi ${trickyName},")
    }

    void 'should handle missing full name'() {
        when:
            EmailContent content = buildActivationEmail(null, link)
        then:
            content.html().contains('Hi ,')
            content.plainText().contains('Hi ,')
    }

}
