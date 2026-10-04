package io.osvaldas.backoffice.domain.clients;

import static io.osvaldas.api.clients.Status.ACTIVE;
import static io.osvaldas.api.clients.Status.DELETED;
import static io.osvaldas.api.clients.Status.REGISTERED;
import static io.osvaldas.api.util.ExceptionMessages.ACTIVATION_LINK_INVALID;
import static io.osvaldas.api.util.ExceptionMessages.CLIENT_ALREADY_EXIST;
import static io.osvaldas.api.util.ExceptionMessages.CLIENT_NOT_FOUND;
import static io.osvaldas.backoffice.repositories.specifications.ClientSpecifications.clientStatusIs;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.api.clients.Status;
import io.osvaldas.api.email.EmailMessage;
import io.osvaldas.api.exceptions.BadRequestException;
import io.osvaldas.api.exceptions.NotFoundException;
import io.osvaldas.backoffice.domain.notifications.NotificationOutboxService;
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig;
import io.osvaldas.backoffice.repositories.ClientRepository;
import io.osvaldas.backoffice.repositories.entities.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    private final NotificationOutboxService notificationOutbox;

    private final Clock clock;

    private final PropertiesConfig config;

    @Transactional
    public Client registerClient(Client client) {
        if (clientRepository.existsByPersonalCode(client.getPersonalCode())) {
            throw new BadRequestException(CLIENT_ALREADY_EXIST);
        }
        client.setRandomId();
        String activationToken = ActivationTokens.newToken();
        client.setActivationTokenHash(ActivationTokens.hash(activationToken));
        client.setActivationTokenExpiresAt(ZonedDateTime.now(clock).plus(config.getActivationTokenTtl()));
        Client savedClient = clientRepository.saveAndFlush(client);
        log.info("Client registered: {}", savedClient.getId());
        notificationOutbox.enqueue(new EmailMessage(savedClient.getId(), savedClient.getFullName(), savedClient.getEmail(), activationToken));
        return savedClient;
    }

    @Transactional(readOnly = true)
    public Page<Client> getClients(int page, int size) {
        return clientRepository.findAll(pageRequest(page, size));
    }

    @Transactional(readOnly = true)
    public Page<Client> getClientsByStatus(Status status, int page, int size) {
        return clientRepository.findAll(clientStatusIs(status), pageRequest(page, size));
    }

    @Transactional(readOnly = true)
    public Client getClient(String id) {
        return findClient(id);
    }

    @Transactional(propagation = MANDATORY)
    public Client getClientForUpdate(String id) {
        return clientRepository.findForUpdateById(id)
            .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND.formatted(id)));
    }

    @Transactional
    public Client updateClient(Client changes) {
        String id = changes.getId();
        log.info("Updating client: {}", id);
        Client client = findClient(id);
        if (!Objects.equals(client.getVersion(), changes.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Client.class, id);
        }
        client.setFirstName(changes.getFirstName());
        client.setLastName(changes.getLastName());
        client.setEmail(changes.getEmail());
        client.setPhoneNumber(changes.getPhoneNumber());
        return clientRepository.saveAndFlush(client);
    }

    @Transactional
    public void deleteClient(String id) {
        changeClientStatus(id, DELETED);
    }

    @Transactional
    public void activateClient(String id, String token) {
        Client client = findClient(id);
        if (!canBeActivatedWith(client, token)) {
            throw new BadRequestException(ACTIVATION_LINK_INVALID);
        }
        log.info("Activating client: {}", id);
        client.setStatus(ACTIVE);
        client.setActivationTokenHash(null);
        client.setActivationTokenExpiresAt(null);
    }

    private boolean canBeActivatedWith(Client client, String token) {
        return REGISTERED == client.getStatus()
            && client.getActivationTokenExpiresAt() != null
            && client.getActivationTokenExpiresAt().isAfter(ZonedDateTime.now(clock))
            && ActivationTokens.matches(token, client.getActivationTokenHash());
    }

    private void changeClientStatus(String id, Status status) {
        log.info("Changing client: {} status to: {}", id, status);
        findClient(id).setStatus(status);
    }

    private static Pageable pageRequest(int page, int size) {
        return PageRequest.of(page, size, Sort.by("lastName").descending());
    }

    private Client findClient(String id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND.formatted(id)));
    }

}
