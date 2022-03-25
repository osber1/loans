package io.osvaldas.loans.domain.clients;

import static java.lang.String.format;
import static java.util.Optional.of;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.loans.domain.exceptions.BadRequestException;
import io.osvaldas.loans.domain.exceptions.NotFoundException;
import io.osvaldas.loans.repositories.ClientRepository;
import io.osvaldas.loans.repositories.entities.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Value("${exceptionMessages.clientErrorMessage:}")
    private String clientErrorMessage;

    @Transactional
    public Client registerClient(Client client) {
        return of(clientRepository.existsByPersonalCode(client.getPersonalCode()))
            .filter(exists -> !exists)
            .map(s -> saveNewClient(client))
            .orElseThrow(() -> new BadRequestException("Client with personal code already exists."));

    }

    private Client saveNewClient(Client client) {
        client.setRandomId();
        return save(client);
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
        of(clientRepository.existsById(id))
            .filter(exists -> exists)
            .ifPresentOrElse(s -> clientRepository.deleteById(id), () -> {
                throw new NotFoundException(format(clientErrorMessage, id));
            });
    }

    @Transactional
    public Client updateClient(Client client) {
        String id = client.getId();
        return of(clientRepository.existsById(id))
            .filter(exists -> exists)
            .map(s -> save(client))
            .orElseThrow(() -> new NotFoundException(format(clientErrorMessage, id)));
    }

    public Client save(Client client) {
        return clientRepository.save(client);
    }
}
