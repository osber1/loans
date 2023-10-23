package io.osvaldas.backoffice.infra.rest.clients

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.clients.Status.DELETED
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
import io.osvaldas.api.clients.ClientRegisterRequest
import io.osvaldas.api.clients.ClientResponse
import io.osvaldas.api.clients.ClientUpdateRequest
import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.Client
import spock.lang.Shared

class ClientControllerSpec extends AbstractControllerSpec {

    @Shared
    String editedName = 'editedName'

    @Shared
    String editedSurname = 'editedSurname'

    void 'should register new client when everything is valid'() {
        given:
            ClientRegisterRequest clientRequest = buildRegisterClientRequest()
        when:
            MvcResult result = sendRegistrationClientRequest(clientRequest)
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

    @SuppressWarnings('LineLength')
    void 'should throw exception with message #errorMessage when validating client'() {
        given:
            ClientRegisterRequest clientRequest = request
        when:
            MvcResult result = sendRegistrationClientRequest(clientRequest)
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
            Client savedClient = clientRepository.save(registeredClientWithId)
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/clients/{id}', savedClient.id)
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

    void 'should change client status to deleted client when it exists'() {
        given:
            Client savedClient = clientRepository.save(registeredClientWithId)
        when:
            MockHttpServletResponse response = mockMvc.perform(delete('/api/v1/clients/{id}', savedClient.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            clientRepository.findById(savedClient.id).get().status == DELETED
    }

    void 'should update client when it exists'() {
        given:
            clientRepository.save(registeredClientWithId)
        when:
            MockHttpServletResponse response = sendUpdateRequest().response
        then:
            response.status == OK.value()
        and:
            with(clientRepository.findById(registeredClientWithId.id).get()) {
                firstName == editedName
                lastName == editedSurname
            }
    }

    void 'should throw when updating client with incorrect version'() {
        given:
            clientRepository.save(registeredClientWithId)
        and:
            sendUpdateRequest()
        when:
            sendUpdateRequest()
        then:
            Exception e = thrown()
            e.message.contains('Row was updated or deleted by another transaction')
    }

    void 'should get list of clients when they exists'() {
        given:
            clientRepository.save(buildClient('123123123', [] as Set, ACTIVE))
            clientRepository.save(buildClient('890890890', [] as Set, ACTIVE))
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/clients')
                .param('page', '0')
                .param('size', '10')
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            List.of(objectMapper.readValue(response.contentAsString, ClientResponse[])).size() == 2
    }

    void 'should get list of clients by status'() {
        given:
            clientRepository.save(buildClient('123123123', [] as Set, ACTIVE))
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/clients/status')
                .param('status', status.name())
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            List.of(objectMapper.readValue(response.contentAsString, ClientResponse[])).size() == listSize
        where:
            status  || listSize
            ACTIVE  || 1
            DELETED || 0
    }

    void 'should throw an exception when client not found'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(method
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == NOT_FOUND.value()
        and:
            response.contentAsString.contains(clientNotFound.formatted(clientId))
        where:
            method << [get('/api/v1/clients/{id}', clientId),
                       delete('/api/v1/clients/{id}', clientId),
                       put('/api/v1/clients').content(new JsonBuilder(buildUpdateClientRequest()) as String)]
    }

    void 'should activate client when it exists'() {
        given:
            clientRepository.save(registeredClientWithId)
        when:
            MockHttpServletResponse response = mockMvc
                .perform(get('/api/v1/clients/{id}/active', registeredClientWithId.id)
                    .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            with(clientRepository.findById(registeredClientWithId.id).get()) {
                status == ACTIVE
            }
    }

    private MvcResult sendRegistrationClientRequest(ClientRegisterRequest request) {
        mockMvc.perform(post('/api/v1/clients')
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn()
    }

    private MvcResult sendUpdateRequest() {
        mockMvc.perform(put('/api/v1/clients')
            .content(new JsonBuilder(buildUpdateClientRequest()) as String)
            .contentType(APPLICATION_JSON))
            .andReturn()
    }

    private ClientRegisterRequest buildRegisterClientRequest() {
        new ClientRegisterRequest().tap {
            firstName = name
            lastName = surname
            personalCode = clientPersonalCode
            email = clientEmail
            phoneNumber = clientPhoneNumber
        }
    }

    private ClientUpdateRequest buildUpdateClientRequest() {
        new ClientUpdateRequest().tap {
            id = clientId
            firstName = editedName
            lastName = editedSurname
            personalCode = clientPersonalCode
            email = clientEmail
            phoneNumber = clientPhoneNumber
            version = 0
        }
    }

}
