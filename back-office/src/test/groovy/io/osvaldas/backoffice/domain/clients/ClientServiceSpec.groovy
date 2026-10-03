package io.osvaldas.backoffice.domain.clients

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.clients.Status.DELETED
import static io.osvaldas.api.clients.Status.REGISTERED
import static java.util.Optional.empty
import static java.util.Optional.of

import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.orm.ObjectOptimisticLockingFailureException

import io.osvaldas.api.exceptions.BadRequestException
import io.osvaldas.api.exceptions.NotFoundException
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.repositories.ClientRepository
import io.osvaldas.backoffice.repositories.entities.Client
import spock.lang.Subject

class ClientServiceSpec extends AbstractSpec {

    static final String OTHER_PERSONAL_CODE = '99999999999'

    ClientRepository clientRepository = Mock()

    ApplicationEventPublisher eventPublisher = Mock()

    @Subject
    ClientService clientService = new ClientService(clientRepository, eventPublisher)

    void 'should throw exception when registering client with existing personal code'() {
        when:
            clientService.registerClient(registeredClientWithoutId)
        then:
            BadRequestException e = thrown()
            e.message == CLIENT_ALREADY_EXIST
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> true
            0 * clientRepository.saveAndFlush(_)
            0 * eventPublisher.publishEvent(_)
    }

    void 'should register new client and publish registration event when client with new personal code'() {
        when:
            Client registeredClient = clientService.registerClient(registeredClientWithoutId)
        then:
            registeredClient.id == CLIENT_ID
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> false
            1 * clientRepository.saveAndFlush(registeredClientWithoutId) >> registeredClientWithId
            1 * eventPublisher.publishEvent(new ClientRegisteredEvent(CLIENT_ID, NAME + ' ' + SURNAME, CLIENT_EMAIL))
    }

    void 'should not publish registration event when saving client fails'() {
        when:
            clientService.registerClient(registeredClientWithoutId)
        then:
            thrown(IllegalStateException)
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> false
            1 * clientRepository.saveAndFlush(registeredClientWithoutId) >> { throw new IllegalStateException() }
            0 * eventPublisher.publishEvent(_)
    }

    void 'should return clients list when there are clients'() {
        when:
            Collection clients = clientService.getClients(0, 2)
        then:
            clients.size() == 2
        and:
            clients == [registeredClientWithId, registeredClientWithId]
        and:
            1 * clientRepository.findAll(_ as Pageable)
                >> new PageImpl<>([registeredClientWithId, registeredClientWithId], Pageable.unpaged(), 1)
    }

    void 'should return empty list when there are no clients'() {
        when:
            Collection clients = clientService.getClients(0, 2)
        then:
            clients == []
        and:
            1 * clientRepository.findAll(_ as Pageable) >> Page.empty()
    }

    void 'should return client when it exists'() {
        when:
            Client client = clientService.getClient(CLIENT_ID)
        then:
            client.id == CLIENT_ID
        and:
            1 * clientRepository.findById(CLIENT_ID) >> of(registeredClientWithId)
    }

    void 'should throw exception when trying to get non existing client'() {
        when:
            clientService.getClient(CLIENT_ID)
        then:
            NotFoundException e = thrown()
            e.message == CLIENT_NOT_FOUND
        and:
            1 * clientRepository.findById(CLIENT_ID) >> empty()
    }

    void 'should change client status to #newStatus when it exists'() {
        given:
            Client client = buildClient(CLIENT_ID, [] as Set, REGISTERED)
        when:
            action(clientService)
        then:
            client.status == newStatus
        and:
            1 * clientRepository.findById(CLIENT_ID) >> of(client)
            0 * clientRepository.save(_)
        where:
            newStatus || action
            DELETED   || { ClientService service -> service.deleteClient(CLIENT_ID) }
            ACTIVE    || { ClientService service -> service.activateClient(CLIENT_ID) }
    }

    void 'should throw exception when trying to change status of non existing client'() {
        when:
            action(clientService)
        then:
            NotFoundException e = thrown()
            e.message == CLIENT_NOT_FOUND
        and:
            1 * clientRepository.findById(CLIENT_ID) >> empty()
        where:
            action << [{ ClientService service -> service.deleteClient(CLIENT_ID) },
                       { ClientService service -> service.activateClient(CLIENT_ID) }]
    }

    void 'should update only editable client fields when it exists'() {
        given:
            Client storedClient = buildClient(CLIENT_ID, [loan] as Set, REGISTERED).tap {
                version = 3L
            }
        and:
            Client changes = new Client().tap {
                id = CLIENT_ID
                firstName = 'newName'
                lastName = 'newSurname'
                email = 'new@mail.com'
                phoneNumber = '37060000000'
                personalCode = OTHER_PERSONAL_CODE
                status = ACTIVE
                version = 3L
            }
        when:
            Client updatedClient = clientService.updateClient(changes)
        then:
            updatedClient.is(storedClient)
        and:
            with(updatedClient) {
                firstName == 'newName'
                lastName == 'newSurname'
                email == 'new@mail.com'
                phoneNumber == '37060000000'
                personalCode == CLIENT_PERSONAL_CODE
                status == REGISTERED
                loans == [loan] as Set
            }
        and:
            1 * clientRepository.findById(CLIENT_ID) >> of(storedClient)
            1 * clientRepository.saveAndFlush(storedClient) >> storedClient
    }

    void 'should throw optimistic locking exception when updating client with stale version'() {
        given:
            Client storedClient = buildClient(CLIENT_ID, [] as Set, REGISTERED).tap {
                version = 1L
            }
        and:
            Client changes = buildClient(CLIENT_ID, [] as Set, REGISTERED).tap {
                firstName = 'newName'
                version = 0L
            }
        when:
            clientService.updateClient(changes)
        then:
            thrown(ObjectOptimisticLockingFailureException)
        and:
            storedClient.firstName == NAME
        and:
            1 * clientRepository.findById(CLIENT_ID) >> of(storedClient)
            0 * clientRepository.saveAndFlush(_)
    }

    void 'should throw exception when trying to update non existing client'() {
        when:
            clientService.updateClient(registeredClientWithId)
        then:
            NotFoundException e = thrown()
            e.message == CLIENT_NOT_FOUND
        and:
            1 * clientRepository.findById(CLIENT_ID) >> empty()
            0 * clientRepository.saveAndFlush(_)
    }

    void 'should return all clients by status'() {
        when:
            Collection clients = clientService.getClientsByStatus(ACTIVE)
        then:
            clients.size() == 1
        and:
            clients == [registeredClientWithId]
        and:
            1 * clientRepository.findAll(_ as Specification) >> [registeredClientWithId]
    }

}
