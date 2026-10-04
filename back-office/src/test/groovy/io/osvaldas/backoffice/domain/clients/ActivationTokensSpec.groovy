package io.osvaldas.backoffice.domain.clients

import spock.lang.Specification

class ActivationTokensSpec extends Specification {

    void 'should generate unique url safe tokens'() {
        when:
            List<String> tokens = (1..100).collect { ActivationTokens.newToken() }
        then:
            tokens.toSet().size() == 100
            tokens.every { it ==~ /[A-Za-z0-9_-]{43}/ }
    }

    void 'should hash a token to a stable sha-256 hex string'() {
        expect:
            ActivationTokens.hash('token') == '3c469e9d6c5875d37a43f353d4f88e61fcf812c66eee3457465a40b0da4153e0'
    }

    void 'should match only the token the hash was made from'() {
        given:
            String hash = ActivationTokens.hash('token')
        expect:
            ActivationTokens.matches('token', hash)
            !ActivationTokens.matches('other', hash)
            !ActivationTokens.matches(null, hash)
            !ActivationTokens.matches('token', null)
    }

}
