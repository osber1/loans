package io.osvaldas.backoffice.domain.clients

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.clients.Status.DELETED
import static java.util.Optional.empty
import static java.util.Optional.of

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification

import io.osvaldas.api.email.EmailMessage
import io.osvaldas.api.exceptions.BadRequestException
import io.osvaldas.api.exceptions.NotFoundException
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.repositories.ClientRepository
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.messages.RabbitMQMessageProducer
import io.osvaldas.messages.RabbitProperties
import io.osvaldas.messages.RabbitProperties.Exchanges
import io.osvaldas.messages.RabbitProperties.RoutingKeys
import spock.lang.Subject

class ClientServiceSpec extends AbstractSpec {

    ClientRepository clientRepository = Mock()

    RabbitMQMessageProducer messageProducer = Mock()

    RabbitProperties rabbitProperties = Stub {
        exchanges >> Stub(Exchanges) {
            internal >> 'internal.exchange'
        }
        routingKeys >> Stub(RoutingKeys) {
            internalNotification >> 'internal.notification.routing-key'
        }
    }

    @Subject
    ClientService clientService = new ClientService(clientRepository, messageProducer, rabbitProperties)

    void 'should throw exception when registering client with existing personal code'() {
        when:
            clientService.registerClient(registeredClientWithoutId)
        then:
            BadRequestException e = thrown()
            e.message == CLIENT_ALREADY_EXIST
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> true
            0 * messageProducer.publish(_ as EmailMessage, _ as String, _ as String)
    }

    void 'should register new client when client with new personal code'() {
        when:
            Client registeredClient = clientService.registerClient(registeredClientWithoutId)
        then:
            registeredClient.id == CLIENT_ID
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> false
            1 * clientRepository.save(registeredClientWithoutId) >> registeredClientWithId
            1 * messageProducer.publish(_ as EmailMessage, _ as String, _ as String)
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

    void 'should change client status to deleted when it exists'() {
        when:
            clientService.deleteClient(CLIENT_ID)
        then:
            1 * clientRepository.changeClientStatus(CLIENT_ID, DELETED)
        and:
            1 * clientRepository.existsById(CLIENT_ID) >> true
    }

    void 'should throw exception when trying to delete non existing client'() {
        when:
            clientService.deleteClient(CLIENT_ID)
        then:
            NotFoundException e = thrown()
            e.message == CLIENT_NOT_FOUND
        and:
            0 * clientRepository.changeClientStatus(CLIENT_ID, DELETED)
        and:
            1 * clientRepository.existsById(CLIENT_ID) >> false
    }

    void 'should update client when it exists'() {
        when:
            clientService.updateClient(registeredClientWithId)
        then:
            1 * clientRepository.save(registeredClientWithId) >> registeredClientWithId
        and:
            1 * clientRepository.existsById(CLIENT_ID) >> true
    }

    void 'should throw exception when trying to update non existing client'() {
        when:
            clientService.updateClient(registeredClientWithId)
        then:
            NotFoundException e = thrown()
            e.message == CLIENT_NOT_FOUND
        and:
            0 * clientRepository.save(registeredClientWithId)
        and:
            1 * clientRepository.existsById(CLIENT_ID) >> false
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
