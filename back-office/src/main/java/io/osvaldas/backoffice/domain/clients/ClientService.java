package io.osvaldas.backoffice.domain.clients;

import static io.osvaldas.api.clients.Status.ACTIVE;
import static io.osvaldas.api.clients.Status.DELETED;
import static io.osvaldas.api.util.ExceptionMessages.CLIENT_ALREADY_EXIST;
import static io.osvaldas.api.util.ExceptionMessages.CLIENT_NOT_FOUND;
import static io.osvaldas.backoffice.repositories.specifications.ClientSpecifications.clientStatusIs;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.api.clients.Status;
import io.osvaldas.api.exceptions.BadRequestException;
import io.osvaldas.api.exceptions.NotFoundException;
import io.osvaldas.backoffice.repositories.ClientRepository;
import io.osvaldas.backoffice.repositories.entities.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Registers a new client. The existence check gives a friendly error for the common case, while the unique
     * constraint on {@code client.personal_code} guards against concurrent registrations (mapped to HTTP 409).
     * The notification is sent only after the transaction commits.
     */
    @Transactional
    public Client registerClient(Client client) {
        if (clientRepository.existsByPersonalCode(client.getPersonalCode())) {
            throw new BadRequestException(CLIENT_ALREADY_EXIST);
        }
        client.setRandomId();
        Client savedClient = clientRepository.saveAndFlush(client);
        log.info("Client registered: {}", savedClient.getId());
        eventPublisher.publishEvent(new ClientRegisteredEvent(savedClient.getId(), savedClient.getFullName(), savedClient.getEmail()));
        return savedClient;
    }

    @Transactional(readOnly = true)
    public Collection<Client> getClients(int page, int size) {
        Pageable pageRequest = PageRequest.of(page, size, Sort.by("lastName").descending());
        return clientRepository.findAll(pageRequest).getContent();
    }

    @Transactional(readOnly = true)
    public List<Client> getClientsByStatus(Status status) {
        return clientRepository.findAll(clientStatusIs(status));
    }

    @Transactional(readOnly = true)
    public Client getClient(String id) {
        return findClient(id);
    }

    /**
     * Updates the editable details of an existing client. Status, personal code, creation date and loans are never
     * taken from the request; they change only through dedicated operations.
     *
     * @param changes detached client carrying the requested values and the version the caller has seen
     * @return the managed, updated client
     */
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
    public void activateClient(String id) {
        changeClientStatus(id, ACTIVE);
    }

    @Transactional
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    private void changeClientStatus(String id, Status status) {
        log.info("Changing client: {} status to: {}", id, status);
        findClient(id).setStatus(status);
    }

    private Client findClient(String id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(CLIENT_NOT_FOUND.formatted(id)));
    }

}
