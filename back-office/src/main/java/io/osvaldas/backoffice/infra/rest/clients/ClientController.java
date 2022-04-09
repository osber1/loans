package io.osvaldas.backoffice.infra.rest.clients;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.backoffice.domain.clients.ClientService;
import io.osvaldas.backoffice.infra.rest.clients.dtos.ClientRegisterRequest;
import io.osvaldas.backoffice.infra.rest.clients.dtos.ClientResponse;
import io.osvaldas.backoffice.infra.rest.clients.dtos.ClientUpdateRequest;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.backoffice.repositories.mapper.ClientMapper;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class ClientController {

    private final ClientService service;

    private final ClientMapper clientMapper;

    @PostMapping("client")
    public ClientResponse registerClient(@Valid @RequestBody ClientRegisterRequest request) {
        Client client = clientMapper.clientRegisterToEntity(request);
        return clientMapper.clientToDTO(service.registerClient(client));
    }

    @GetMapping("clients")
    public Collection<ClientResponse> getClients() {
        return clientMapper.clientsToDTOs(service.getClients());
    }

    @GetMapping("client/{id}")
    public ClientResponse getClient(@PathVariable String id) {
        return clientMapper.clientToDTO(service.getClient(id));
    }

    @PutMapping("client")
    public ClientResponse updateClient(@Valid @RequestBody ClientUpdateRequest request) {
        Client client = clientMapper.clientUpdateToEntity(request);
        return clientMapper.clientToDTO(service.updateClient(client));
    }

    @DeleteMapping("client/{id}")
    public void deleteClient(@PathVariable String id) {
        service.deleteClient(id);
    }

    @PostMapping("client/{id}/inactive")
    public void inactivateClient(@PathVariable String id) {
        service.inactivateClient(id);
    }

    @GetMapping("client/{id}/active")
    public void activateClient(@PathVariable String id) {
        service.activateClient(id);
    }
}
