package com.finance.interest.service

import com.finance.interest.configuration.PropertiesConfig
import com.finance.interest.model.ClientDAO
import com.finance.interest.repository.ClientRepository
import com.finance.interest.repository.LoanRepository
import com.finance.interest.util.ValidationUtils

import spock.lang.Specification

class ClientServiceTest extends Specification {

    def clientRepository = Mock(ClientRepository)

    def loanRepository = Mock(LoanRepository)

    def config = Mock(PropertiesConfig)

    def validationUtils = Mock(ValidationUtils)

    def clientService = new ClientService(clientRepository, loanRepository, config, validationUtils)

    def client = createClient()

    void 'test'() {
        when:
            clientRepository.findById(_ as String) >> client

        then:
            clientService.returnName("Tomas") == "Tomas"
    }

    static Optional<ClientDAO> createClient() {
        ClientDAO client = new ClientDAO()
        client.setFirstName("Tomas")
        return Optional.of(client)
    }
}