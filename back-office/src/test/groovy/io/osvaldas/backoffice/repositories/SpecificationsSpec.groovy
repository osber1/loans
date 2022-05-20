package io.osvaldas.backoffice.repositories

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.clients.Status.DELETED
import static io.osvaldas.backoffice.repositories.Specifications.statusIs

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification

import io.osvaldas.backoffice.repositories.entities.Client
import spock.lang.Subject

class SpecificationsSpec extends AbstractDatabaseSpec {

    @Subject
    @Autowired
    ClientRepository repository

    void setup() {
        client.addLoan(loan)
        repository.save(client)
    }

    void 'should change client status to deleted when deleting client'() {
        when:
            List<Client> clients = repository.findAll(Specification.where(statusIs(status)))
        then:
            clients.size() == listSize
        where:
            status  || listSize
            ACTIVE  || 1
            DELETED || 0
    }

}
