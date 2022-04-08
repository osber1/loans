package io.osvaldas.backoffice.repositories.entities

import spock.lang.Specification
import spock.lang.Subject

class ClientSpec extends Specification {

    @Subject
    Client client = new Client(firstName: 'John', lastName: 'Doe')

    void 'should set client ID'() {
        when:
            client.setRandomId()
        then:
            client.id
    }

    void 'should return full client name'() {
        expect:
            client.fullName == "John Doe"
    }
}
