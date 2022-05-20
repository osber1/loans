package io.osvaldas.backoffice.domain.clients;

import static io.osvaldas.api.clients.Status.ACTIVE;
import static io.osvaldas.api.clients.Status.DELETED;
import static io.osvaldas.api.clients.Status.INACTIVE;
import static java.lang.String.format;
import static java.util.Optional.of;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.api.clients.Status;
import io.osvaldas.api.email.EmailMessage;
import io.osvaldas.api.exceptions.BadRequestException;
import io.osvaldas.api.exceptions.NotFoundException;
import io.osvaldas.backoffice.repositories.ClientRepository;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.messages.RabbitMQMessageProducer;
import io.osvaldas.messages.RabbitProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    private final RabbitMQMessageProducer messageProducer;

    private final RabbitProperties rabbitProperties;

    @Value("${exceptionMessages.clientAlreadyExistErrorMessage:}")
    private String clientAlreadyExistErrorMessage;

    @Value("${exceptionMessages.clientErrorMessage:}")
    private String clientErrorMessage;

    @Transactional
    public Client registerClient(Client client) {
        return of(clientRepository.existsByPersonalCode(client.getPersonalCode()))
            .filter(exists -> !exists)
            .map(s -> saveClientAndSendEmail(client))
            .orElseThrow(() -> new BadRequestException(clientAlreadyExistErrorMessage));
    }

    @Transactional(readOnly = true)
    public Collection<Client> getClients(int page, int size) {
        Pageable pageRequest = PageRequest.of(page, size, Sort.by("lastName").descending());
        return clientRepository.findAll(pageRequest).getContent();
    }

    @Transactional(readOnly = true)
    public Client getClient(String id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(format(clientErrorMessage, id)));
    }

    @Transactional
    public Client updateClient(Client client) {
        log.info("Updating client: {}", client.getId());
        String id = client.getId();
        return clientExists(id)
            .map(s -> save(client))
            .orElseThrow(() -> new NotFoundException(format(clientErrorMessage, id)));
    }

    @Transactional
    public void deleteClient(String id) {
        changeClientStatusIfExists(id, DELETED);
    }

    @Transactional
    public void inactivateClient(String id) {
        changeClientStatusIfExists(id, INACTIVE);
    }

    @Transactional
    public void activateClient(String id) {
        changeClientStatusIfExists(id, ACTIVE);
    }

    @Transactional
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public int getLoanTakenTodayCount(String clientId, ZonedDateTime date) {
        return clientRepository.countByIdAndLoansCreatedAtAfter(clientId, date);
    }

    private Client saveClientAndSendEmail(Client client) {
        Client savedClient = saveNewClient(client);
        sendMessage(savedClient);
        return savedClient;
    }

    private void sendMessage(Client client) {
        EmailMessage message = new EmailMessage(client.getId(), client.getFullName(), client.getEmail());
        messageProducer.publish(message, rabbitProperties.getExchanges().getInternal(), rabbitProperties.getRoutingKeys().getInternalNotification());
    }

    private Client saveNewClient(Client client) {
        client.setRandomId();
        log.info("Client registered: {}", client.getId());
        return save(client);
    }

    private void changeClientStatusIfExists(String id, Status status) {
        log.info("Changing client: {} status to: {}", id, status);
        clientExists(id)
            .ifPresentOrElse(s -> clientRepository.changeClientStatus(id, status), () -> {
                throw new NotFoundException(format(clientErrorMessage, id));
            });
    }

    private Optional<Boolean> clientExists(String id) {
        return of(clientRepository.existsById(id))
            .filter(exists -> exists);
    }

}
