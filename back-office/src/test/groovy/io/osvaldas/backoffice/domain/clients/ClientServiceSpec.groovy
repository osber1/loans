package io.osvaldas.backoffice.domain.clients

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.clients.Status.DELETED
import static io.osvaldas.api.clients.Status.REGISTERED
import static io.osvaldas.api.util.ExceptionMessages.ACTIVATION_LINK_INVALID
import static java.util.Optional.empty
import static java.util.Optional.of

import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.orm.ObjectOptimisticLockingFailureException

import io.osvaldas.api.email.EmailMessage
import io.osvaldas.api.exceptions.BadRequestException
import io.osvaldas.api.exceptions.NotFoundException
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.notifications.NotificationOutboxService
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig
import io.osvaldas.backoffice.repositories.ClientRepository
import io.osvaldas.backoffice.repositories.entities.Client
import spock.lang.Subject

class ClientServiceSpec extends AbstractSpec {

    static final String OTHER_PERSONAL_CODE = '99999999999'

    static final String TOKEN = 'activation-token'

    ClientRepository clientRepository = Mock()

    NotificationOutboxService notificationOutbox = Mock()

    Clock clock = Clock.fixed(DATE.toInstant(), DATE.zone)

    PropertiesConfig config = Stub {
        activationTokenTtl >> Duration.ofDays(7)
    }

    @Subject
    ClientService clientService = new ClientService(clientRepository, notificationOutbox, clock, config)

    void 'should throw exception when registering client with existing personal code'() {
        when:
            clientService.registerClient(registeredClientWithoutId)
        then:
            BadRequestException e = thrown()
            e.message == CLIENT_ALREADY_EXIST
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> true
            0 * clientRepository.saveAndFlush(_)
            0 * notificationOutbox.enqueue(_)
    }

    void 'should register new client and enqueue registration email when client with new personal code'() {
        when:
            Client registeredClient = clientService.registerClient(registeredClientWithoutId)
        then:
            registeredClient.id == CLIENT_ID
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> false
            1 * clientRepository.saveAndFlush(registeredClientWithoutId) >> registeredClientWithId
            1 * notificationOutbox.enqueue(_ as EmailMessage) >> { EmailMessage message ->
                assert message.clientId() == CLIENT_ID
                assert message.fullName() == NAME + ' ' + SURNAME
                assert message.email() == CLIENT_EMAIL
                assert message.activationToken() != null
            }
    }

    void 'should store only the hash and expiry of the activation token that is sent by email'() {
        given:
            Client client = registeredClientWithoutId
            EmailMessage sent
        when:
            clientService.registerClient(client)
        then:
            1 * clientRepository.existsByPersonalCode(client.personalCode) >> false
            1 * clientRepository.saveAndFlush(client) >> registeredClientWithId
            1 * notificationOutbox.enqueue(_ as EmailMessage) >> { EmailMessage message -> sent = message }
        and:
            client.activationTokenHash == ActivationTokens.hash(sent.activationToken())
            client.activationTokenHash != sent.activationToken()
            client.activationTokenExpiresAt == DATE.plusDays(7)
    }

    void 'should activate registered client when the token matches'() {
        given:
            Client client = clientWithActivationToken(TOKEN, DATE.plusDays(1))
        when:
            clientService.activateClient(CLIENT_ID, TOKEN)
        then:
            1 * clientRepository.findById(CLIENT_ID) >> of(client)
        and:
            client.status == ACTIVE
            client.activationTokenHash == null
            client.activationTokenExpiresAt == null
    }

    void 'should not activate client when #reason'() {
        given:
            Client client = clientWithActivationToken(TOKEN, expiresAt).tap {
                status = clientStatus
            }
        when:
            clientService.activateClient(CLIENT_ID, token)
        then:
            1 * clientRepository.findById(CLIENT_ID) >> of(client)
        and:
            BadRequestException e = thrown()
            e.message == ACTIVATION_LINK_INVALID
            client.status == clientStatus
        where:
            reason                  | token       | expiresAt          | clientStatus
            'the token is wrong'    | 'other'     | DATE.plusDays(1)   | REGISTERED
            'the token is missing'  | null        | DATE.plusDays(1)   | REGISTERED
            'the token has expired' | TOKEN       | DATE.minusSeconds(1) | REGISTERED
            'the token expires now' | TOKEN       | DATE               | REGISTERED
            'client is deleted'     | TOKEN       | DATE.plusDays(1)   | DELETED
            'client is active'      | TOKEN       | DATE.plusDays(1)   | ACTIVE
    }

    void 'should not activate client that has no activation token'() {
        when:
            clientService.activateClient(CLIENT_ID, TOKEN)
        then:
            1 * clientRepository.findById(CLIENT_ID) >> of(registeredClientWithId)
        and:
            thrown(BadRequestException)
    }

    void 'should not enqueue registration email when saving client fails'() {
        when:
            clientService.registerClient(registeredClientWithoutId)
        then:
            thrown(IllegalStateException)
        and:
            1 * clientRepository.existsByPersonalCode(registeredClientWithoutId.personalCode) >> false
            1 * clientRepository.saveAndFlush(registeredClientWithoutId) >> { throw new IllegalStateException() }
            0 * notificationOutbox.enqueue(_)
    }

    void 'should return clients page when there are clients'() {
        when:
            Page<Client> clients = clientService.getClients(0, 2)
        then:
            clients.content == [registeredClientWithId, registeredClientWithId]
            clients.totalElements == 5
        and:
            1 * clientRepository.findAll(PageRequest.of(0, 2, Sort.by('lastName').descending()))
                >> new PageImpl<>([registeredClientWithId, registeredClientWithId], PageRequest.of(0, 2), 5)
    }

    void 'should return empty page when there are no clients'() {
        when:
            Page<Client> clients = clientService.getClients(0, 2)
        then:
            clients.content == []
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

    void 'should return locked client when getting it for update'() {
        when:
            Client client = clientService.getClientForUpdate(CLIENT_ID)
        then:
            client.is(activeClientWithId)
        and:
            1 * clientRepository.findForUpdateById(CLIENT_ID) >> of(activeClientWithId)
    }

    void 'should throw exception when trying to get non existing client for update'() {
        when:
            clientService.getClientForUpdate(CLIENT_ID)
        then:
            NotFoundException e = thrown()
            e.message == CLIENT_NOT_FOUND
        and:
            1 * clientRepository.findForUpdateById(CLIENT_ID) >> empty()
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
                       { ClientService service -> service.activateClient(CLIENT_ID, TOKEN) }]
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

    void 'should return page of clients by status'() {
        when:
            Page<Client> clients = clientService.getClientsByStatus(ACTIVE, 1, 10)
        then:
            clients.content == [registeredClientWithId]
            clients.totalElements == 11
        and:
            1 * clientRepository.findAll(_ as Specification, PageRequest.of(1, 10, Sort.by('lastName').descending()))
                >> new PageImpl<>([registeredClientWithId], PageRequest.of(1, 10), 11)
    }

    private Client clientWithActivationToken(String token, ZonedDateTime expiresAt) {
        buildClient(CLIENT_ID, [] as Set, REGISTERED).tap {
            activationTokenHash = ActivationTokens.hash(token)
            activationTokenExpiresAt = expiresAt
        }
    }

}
