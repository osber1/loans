package io.osvaldas.backoffice.domain.clients

import static io.osvaldas.api.clients.Status.DELETED
import static io.osvaldas.api.clients.Status.INACTIVE
import static java.util.Optional.empty
import static java.util.Optional.of

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

    ClientRepository clientRepository = Mock {
        countByIdAndLoansCreatedAtAfter(clientId, date) >> 2
    }

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

    void setup() {
        clientService.clientErrorMessage = clientErrorMessage
        clientService.clientAlreadyExistErrorMessage = clientAlreadyExistErrorMessage
    }

    void 'should throw exception when registering client with existing personal code'() {
        when:
            clientService.registerClient(registeredClientWithoutId)
        then:
            BadRequestException e = thrown()
            e.message == 'Client with personal code already exists.'
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> true
            0 * messageProducer.publish(_ as EmailMessage, _ as String, _ as String)
    }

    void 'should register new client when client with new personal code'() {
        when:
            Client registeredClient = clientService.registerClient(registeredClientWithoutId)
        then:
            registeredClient.id == clientId
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> false
            1 * clientRepository.save(registeredClientWithoutId) >> registeredClientWithId
            1 * messageProducer.publish(_ as EmailMessage, _ as String, _ as String)
    }

    void 'should return clients list when there are clients'() {
        when:
            Collection clients = clientService.getClients()
        then:
            clients.size() == 2
        and:
            clients == [registeredClientWithId, registeredClientWithId]
        and:
            1 * clientRepository.findAll() >> [registeredClientWithId, registeredClientWithId]
    }

    void 'should return empty list when there are no clients'() {
        when:
            Collection clients = clientService.getClients()
        then:
            clients == []
        and:
            1 * clientRepository.findAll() >> []
    }

    void 'should return client when it exists'() {
        when:
            Client client = clientService.getClient(clientId)
        then:
            client.id == clientId
        and:
            1 * clientRepository.findById(clientId) >> of(registeredClientWithId)
    }

    void 'should throw exception when trying to get non existing client'() {
        when:
            clientService.getClient(clientId)
        then:
            NotFoundException e = thrown()
            e.message == clientErrorMessage
        and:
            1 * clientRepository.findById(clientId) >> empty()
    }

    void 'should change client status to deleted when it exists'() {
        when:
            clientService.deleteClient(clientId)
        then:
            1 * clientRepository.changeClientStatus(clientId, DELETED)
        and:
            1 * clientRepository.existsById(clientId) >> true
    }

    void 'should change client status to inactive when it exists'() {
        when:
            clientService.inactivateClient(clientId)
        then:
            1 * clientRepository.changeClientStatus(clientId, INACTIVE)
        and:
            1 * clientRepository.existsById(clientId) >> true
    }

    void 'should throw exception when trying to delete non existing client'() {
        when:
            clientService.deleteClient(clientId)
        then:
            NotFoundException e = thrown()
            e.message == clientErrorMessage
        and:
            0 * clientRepository.changeClientStatus(clientId, DELETED)
        and:
            1 * clientRepository.existsById(clientId) >> false
    }

    void 'should update client when it exists'() {
        when:
            clientService.updateClient(registeredClientWithId)
        then:
            1 * clientRepository.save(registeredClientWithId) >> registeredClientWithId
        and:
            1 * clientRepository.existsById(clientId) >> true
    }

    void 'should throw exception when trying to update non existing client'() {
        when:
            clientService.updateClient(registeredClientWithId)
        then:
            NotFoundException e = thrown()
            e.message == clientErrorMessage
        and:
            0 * clientRepository.save(registeredClientWithId)
        and:
            1 * clientRepository.existsById(clientId) >> false
    }

    void 'should return loan taken today count'() {
        when:
            int loansTakenTodayCount = clientService.getLoanTakenTodayCount(clientId, date)
        then:
            loansTakenTodayCount == 2
    }

}
