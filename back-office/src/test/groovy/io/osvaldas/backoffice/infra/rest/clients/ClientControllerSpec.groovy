package io.osvaldas.backoffice.infra.rest.clients

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.clients.Status.DELETED
import static io.osvaldas.api.clients.Status.REGISTERED
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.CONFLICT
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
import io.osvaldas.backoffice.repositories.entities.NotificationOutbox
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
                firstName() == clientRequest.firstName()
                lastName() == clientRequest.lastName()
                personalCode() == "${clientRequest.personalCode()}"
                email() == clientRequest.email()
                phoneNumber() == clientRequest.phoneNumber()
            }
    }

    void 'should store and relay the activation email when registering a client'() {
        given:
            ClientRegisterRequest clientRequest = buildRegisterClientRequest()
        when:
            MvcResult result = sendRegistrationClientRequest(clientRequest)
        then:
            result.response.status == OK.value()
        and:
            List<NotificationOutbox> outbox = notificationOutboxRepository.findAll()
            outbox.size() == 1
            outbox.first().clientId == objectMapper.readValue(result.response.contentAsString, ClientResponse).id()
            outbox.first().email == clientRequest.email()
            outbox.first().publishedAt != null
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
            request                                                                                    || errorMessage
            buildClientRequest(null, SURNAME, CLIENT_PERSONAL_CODE, CLIENT_EMAIL, CLIENT_PHONE_NUMBER) || 'First name must be not empty.'
            buildClientRequest(NAME, null, CLIENT_PERSONAL_CODE, CLIENT_EMAIL, CLIENT_PHONE_NUMBER)    || 'Last name must be not empty.'
            buildClientRequest(NAME, SURNAME, null, CLIENT_EMAIL, CLIENT_PHONE_NUMBER)                 || 'Personal code must be not empty.'
            buildClientRequest(NAME, SURNAME, '123456789', CLIENT_EMAIL, CLIENT_PHONE_NUMBER)          || 'Personal code must be 11 digits length.'
            buildClientRequest(NAME, SURNAME, '123456789as', CLIENT_EMAIL, CLIENT_PHONE_NUMBER)        || 'Personal code must contain only digits.'
            buildClientRequest(NAME, SURNAME, CLIENT_PERSONAL_CODE, null, CLIENT_PHONE_NUMBER)         || 'Email must be not empty.'
            buildClientRequest(NAME, SURNAME, CLIENT_PERSONAL_CODE, 'test', CLIENT_PHONE_NUMBER)       || 'must be a well-formed email address'
            buildClientRequest(NAME, SURNAME, CLIENT_PERSONAL_CODE, CLIENT_EMAIL, null)                || 'Phone number must be not empty.'
    }

    void 'should describe every invalid field in a problem response'() {
        given:
            ClientRegisterRequest clientRequest =
                buildClientRequest(null, null, CLIENT_PERSONAL_CODE, CLIENT_EMAIL, CLIENT_PHONE_NUMBER)
        when:
            MockHttpServletResponse response = sendRegistrationClientRequest(clientRequest).response
        then:
            response.status == BAD_REQUEST.value()
            response.contentType.startsWith('application/problem+json')
        and:
            Map problem = objectMapper.readValue(response.contentAsString, Map)
            problem.errors as Set ==
                ['firstName: First name must be not empty.', 'lastName: Last name must be not empty.'] as Set
    }

    void 'should describe invalid request parameters in a problem response'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/clients').param('size', '0'))
                .andReturn().response
        then:
            response.status == BAD_REQUEST.value()
            response.contentType.startsWith('application/problem+json')
        and:
            objectMapper.readValue(response.contentAsString, Map).errors.any { it.startsWith('size: ') }
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
                id() == savedClient.id
                firstName() == savedClient.firstName
                lastName() == savedClient.lastName
                personalCode() == "${savedClient.personalCode}"
                email() == savedClient.email
                phoneNumber() == savedClient.phoneNumber
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

    void 'should return conflict when updating client with incorrect version'() {
        given:
            clientRepository.save(registeredClientWithId)
        and:
            sendUpdateRequest()
        when:
            MockHttpServletResponse response = sendUpdateRequest().response
        then:
            response.status == CONFLICT.value()
        and:
            response.contentAsString.contains('The resource was modified by another request.')
        and:
            with(clientRepository.findById(registeredClientWithId.id).get()) {
                version == 1L
            }
    }

    void 'should not change status and personal code when updating client'() {
        given:
            clientRepository.save(registeredClientWithId)
        when:
            MockHttpServletResponse response = mockMvc.perform(put('/api/v1/clients')
                .content(new JsonBuilder(updateRequestWithStatusAndPersonalCode) as String)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            with(objectMapper.readValue(response.contentAsString, ClientResponse)) {
                firstName() == editedName
                status() == REGISTERED
                personalCode() == CLIENT_PERSONAL_CODE
                createdAt() != null
                updatedAt() != null
                version() == 1L
            }
        and:
            with(clientRepository.findById(registeredClientWithId.id).get()) {
                status == REGISTERED
                personalCode == CLIENT_PERSONAL_CODE
            }
    }

    void 'should return requested page of clients with total count'() {
        given:
            clientRepository.save(buildClient('123123123', [] as Set, ACTIVE))
            clientRepository.save(buildClient('890890890', [] as Set, ACTIVE).tap { personalCode = '89089089000' })
            clientRepository.save(buildClient('456456456', [] as Set, REGISTERED).tap { personalCode = '45645645600' })
        when:
            MockHttpServletResponse response = mockMvc.perform(get(url)
                .param('page', '1')
                .param('size', '1')
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            List.of(objectMapper.readValue(response.contentAsString, ClientResponse[])).size() == 1
            response.getHeader('X-Total-Count') == totalCount
        where:
            url                                    || totalCount
            '/api/v1/clients'                      || '3'
            '/api/v1/clients/status?status=ACTIVE' || '2'
    }

    void 'should reject page size #size when listing clients from #url'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(get(url)
                .param('size', size)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == BAD_REQUEST.value()
        where:
            url                                    | size
            '/api/v1/clients'                      | '0'
            '/api/v1/clients'                      | '1001'
            '/api/v1/clients/status?status=ACTIVE' | '1001'
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
            response.getHeader('X-Total-Count') == "${listSize}"
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
            response.contentAsString.contains(CLIENT_NOT_FOUND.formatted(CLIENT_ID))
        where:
            method << [get('/api/v1/clients/{id}', CLIENT_ID),
                       delete('/api/v1/clients/{id}', CLIENT_ID),
                       get('/api/v1/clients/{id}/active', CLIENT_ID),
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
                version == 1L
                updatedAt != null
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
        new ClientRegisterRequest(NAME, SURNAME, CLIENT_EMAIL, CLIENT_PHONE_NUMBER, CLIENT_PERSONAL_CODE)
    }

    private Map getUpdateRequestWithStatusAndPersonalCode() {
        [id          : CLIENT_ID,
         firstName   : editedName,
         lastName    : editedSurname,
         email       : CLIENT_EMAIL,
         phoneNumber : CLIENT_PHONE_NUMBER,
         status      : ACTIVE,
         personalCode: '99999999999',
         version     : 0]
    }

    private ClientUpdateRequest buildUpdateClientRequest() {
        new ClientUpdateRequest(CLIENT_ID, editedName, editedSurname, CLIENT_EMAIL, CLIENT_PHONE_NUMBER, 0)
    }

}
