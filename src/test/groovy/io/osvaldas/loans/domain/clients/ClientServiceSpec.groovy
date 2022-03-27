package io.osvaldas.loans.domain.clients

import static java.util.Optional.empty
import static java.util.Optional.of

import io.osvaldas.loans.domain.AbstractServiceSpec
import io.osvaldas.loans.domain.exceptions.BadRequestException
import io.osvaldas.loans.domain.exceptions.NotFoundException
import io.osvaldas.loans.repositories.ClientRepository
import io.osvaldas.loans.repositories.entities.Client
import spock.lang.Subject

class ClientServiceSpec extends AbstractServiceSpec {

    ClientRepository clientRepository = Mock()

    @Subject
    ClientService clientService = new ClientService(clientRepository, clientErrorMessage)

    void 'should throw exception when registering client with existing personal code'() {
        when:
            clientService.registerClient(clientWithoutId)
        then:
            BadRequestException e = thrown()
            e.message == 'Client with personal code already exists.'
        and:
            1 * clientRepository.existsByPersonalCode(clientWithoutId.personalCode) >> true
    }

    void 'should register new client when client with new personal code'() {
        when:
            Client registeredClient = clientService.registerClient(clientWithoutId)
        then:
            registeredClient.id == clientId
        and:
            1 * clientRepository.existsByPersonalCode(clientWithoutId.personalCode) >> false
            1 * clientRepository.save(clientWithoutId) >> clientWithId
    }

    void 'should return clients list when there are clients'() {
        when:
            Collection clients = clientService.getClients()
        then:
            clients.size() == 2
        and:
            clients == [clientWithId, clientWithId]
        and:
            1 * clientRepository.findAll() >> [clientWithId, clientWithId]
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
            1 * clientRepository.findById(clientId) >> of(clientWithId)
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

    void 'should delete client when it exists'() {
        when:
            clientService.deleteClient(clientId)
        then:
            1 * clientRepository.deleteById(clientId)
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
            0 * clientRepository.deleteById(clientId)
        and:
            1 * clientRepository.existsById(clientId) >> false
    }

    void 'should update client when it exists'() {
        when:
            clientService.updateClient(clientWithId)
        then:
            1 * clientRepository.save(clientWithId) >> clientWithId
        and:
            1 * clientRepository.existsById(clientId) >> true
    }

    void 'should throw exception when trying to update non existing client'() {
        when:
            clientService.updateClient(clientWithId)
        then:
            NotFoundException e = thrown()
            e.message == clientErrorMessage
        and:
            0 * clientRepository.save(clientWithId)
        and:
            1 * clientRepository.existsById(clientId) >> false
    }
}