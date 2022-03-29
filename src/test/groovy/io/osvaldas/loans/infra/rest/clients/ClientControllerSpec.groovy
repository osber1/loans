package io.osvaldas.loans.infra.rest.clients

import static java.lang.String.format
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put

import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MvcResult

import groovy.json.JsonBuilder
import io.osvaldas.loans.infra.rest.AbstractControllerSpec
import io.osvaldas.loans.infra.rest.clients.dtos.ClientRequest
import io.osvaldas.loans.infra.rest.clients.dtos.ClientResponse
import io.osvaldas.loans.repositories.entities.Client
import io.osvaldas.loans.repositories.entities.Loan
import spock.lang.Shared

class ClientControllerSpec extends AbstractControllerSpec {

    @Shared
    String editedName = 'editedName'

    @Shared
    String editedSurname = "editedSurname"

    void 'should register new client when everything is valid'() {
        given:
            ClientRequest clientRequest = buildFullClientRequest()
        when:
            MvcResult result = postClientRequest(clientRequest)
        then:
            result.response.status == OK.value()
        and:
            with(objectMapper.readValue(result.response.contentAsString, ClientResponse)) {
                firstName == clientRequest.firstName
                lastName == clientRequest.lastName
                personalCode == "${clientRequest.personalCode}"
                email == clientRequest.email
                phoneNumber == clientRequest.phoneNumber
            }
    }

    void 'should throw exception with message #errorMessage when validating client'() {
        given:
            ClientRequest clientRequest = request
        when:
            MvcResult result = postClientRequest(clientRequest)
        then:
            result.response.status == BAD_REQUEST.value()
        and:
            result.resolvedException.message.contains(errorMessage)
        where:
            request                                                                               || errorMessage
            buildClientRequest(null, surname, clientPersonalCode, clientEmail, clientPhoneNumber) || 'First name must be not empty.'
            buildClientRequest(name, null, clientPersonalCode, clientEmail, clientPhoneNumber)    || 'Last name must be not empty.'
            buildClientRequest(name, surname, null, clientEmail, clientPhoneNumber)               || 'Personal code must be not empty.'
            buildClientRequest(name, surname, '123456789', clientEmail, clientPhoneNumber)        || 'Personal code must be 11 digits length.'
            buildClientRequest(name, surname, '123456789as', clientEmail, clientPhoneNumber)      || 'Personal code must contain only digits.'
            buildClientRequest(name, surname, clientPersonalCode, null, clientPhoneNumber)        || 'Email must be not empty.'
            buildClientRequest(name, surname, clientPersonalCode, 'test', clientPhoneNumber)      || 'must be a well-formed email address'
            buildClientRequest(name, surname, clientPersonalCode, clientEmail, null)              || 'Phone number must be not empty.'
    }

    void 'should return client when it exists'() {
        given:
            Client savedClient = clientRepository.save(clientWithId)
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/client/{id}', savedClient.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            with(objectMapper.readValue(response.contentAsString, ClientResponse)) {
                id == savedClient.id
                firstName == savedClient.firstName
                lastName == savedClient.lastName
                personalCode == "${savedClient.personalCode}"
                email == savedClient.email
                phoneNumber == savedClient.phoneNumber
            }
    }

    void 'should delete client when it exists'() {
        given:
            Client savedClient = clientRepository.save(clientWithId)
        when:
            MockHttpServletResponse response = mockMvc.perform(delete('/api/v1/client/{id}', savedClient.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            clientRepository.findById(savedClient.id).isEmpty()
    }

    void 'should update client when it exists'() {
        given:
            clientRepository.save(clientWithId)
        when:
            MockHttpServletResponse response = mockMvc.perform(put('/api/v1/client')
                .content(new JsonBuilder(buildFullClientRequest()) as String)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            with(clientRepository.findById(clientWithId.id).get()) {
                firstName == editedName
                lastName == editedSurname
            }
    }

    void 'should get list of clients when they exists'() {
        given:
            clientRepository.save(buildClient('123123123', new HashSet<Loan>()))
            clientRepository.save(buildClient('890890890', new HashSet<Loan>()))
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/clients')
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            List.of(objectMapper.readValue(response.contentAsString, ClientResponse[])).size() == 2
    }

    void 'should throw an exception when client not found'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(method
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == status
        and:
            response.contentAsString.contains(format(clientErrorMessage, clientId))
        where:
            method                                                                             || status
            get('/api/v1/client/{id}', clientId)                                               || NOT_FOUND.value()
            delete('/api/v1/client/{id}', clientId)                                            || NOT_FOUND.value()
            put('/api/v1/client').content(new JsonBuilder(buildFullClientRequest()) as String) || NOT_FOUND.value()
    }

    private MvcResult postClientRequest(ClientRequest request) {
        mockMvc.perform(post('/api/v1/client')
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn()
    }

    private ClientRequest buildFullClientRequest() {
        new ClientRequest().tap {
            id = clientId
            firstName = editedName
            lastName = editedSurname
            personalCode = clientPersonalCode
            email = clientEmail
            phoneNumber = clientPhoneNumber
        }
    }
}