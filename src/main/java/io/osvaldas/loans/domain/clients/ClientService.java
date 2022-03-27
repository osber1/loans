package io.osvaldas.loans.domain.clients;

import static java.lang.String.format;
import static java.util.Optional.of;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.loans.domain.exceptions.BadRequestException;
import io.osvaldas.loans.domain.exceptions.NotFoundException;
import io.osvaldas.loans.repositories.ClientRepository;
import io.osvaldas.loans.repositories.entities.Client;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    private final String clientErrorMessage;

    public ClientService(ClientRepository clientRepository,
                         @Value("${exceptionMessages.clientErrorMessage:}") String clientErrorMessage) {
        this.clientRepository = clientRepository;
        this.clientErrorMessage = clientErrorMessage;
    }

    @Transactional
    public Client registerClient(Client client) {
        return of(clientRepository.existsByPersonalCode(client.getPersonalCode()))
            .filter(exists -> !exists)
            .map(s -> saveNewClient(client))
            .orElseThrow(() -> new BadRequestException("Client with personal code already exists."));
    }

    @Transactional(readOnly = true)
    public Collection<Client> getClients() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Client getClient(String id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(format(clientErrorMessage, id)));
    }

    @Transactional
    public void deleteClient(String id) {
        clientExists(id)
            .ifPresentOrElse(s -> clientRepository.deleteById(id), () -> {
                throw new NotFoundException(format(clientErrorMessage, id));
            });
    }

    @Transactional
    public Client updateClient(Client client) {
        String id = client.getId();
        return clientExists(id)
            .map(s -> save(client))
            .orElseThrow(() -> new NotFoundException(format(clientErrorMessage, id)));
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }

    private Optional<Boolean> clientExists(String id) {
        return of(clientRepository.existsById(id))
            .filter(exists -> exists);
    }

    private Client saveNewClient(Client client) {
        client.setRandomId();
        return save(client);
    }
}
