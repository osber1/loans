package io.osvaldas.backoffice.infra.rest.clients;

import java.util.Collection;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.api.clients.ClientRegisterRequest;
import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.clients.ClientUpdateRequest;
import io.osvaldas.api.clients.Status;
import io.osvaldas.backoffice.domain.clients.ClientService;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.backoffice.repositories.mapper.ClientMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class ClientController {

    private final ClientService service;

    private final ClientMapper clientMapper;

    @PostMapping("clients")
    public ClientResponse registerClient(@Valid @RequestBody ClientRegisterRequest request) {
        Client client = clientMapper.map(request);
        return clientMapper.map(service.registerClient(client));
    }

    @GetMapping("clients")
    public Collection<ClientResponse> getClients(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                 @RequestParam(defaultValue = "20") @Min(1) @Max(1000) int size) {
        return clientMapper.map(service.getClients(page, size));
    }

    @GetMapping("clients/status")
    public Collection<ClientResponse> getClientsByStatus(@RequestParam Status status) {
        return clientMapper.map(service.getClientsByStatus(status));
    }

    @GetMapping("clients/{id}")
    public ClientResponse getClient(@PathVariable String id) {
        return clientMapper.map(service.getClient(id));
    }

    @PutMapping("clients")
    public ClientResponse updateClient(@Valid @RequestBody ClientUpdateRequest request) {
        Client client = clientMapper.mapToEntity(request);
        return clientMapper.map(service.updateClient(client));
    }

    @DeleteMapping("clients/{id}")
    public void deleteClient(@PathVariable String id) {
        service.deleteClient(id);
    }

    @GetMapping("clients/{id}/active")
    public void activateClient(@PathVariable String id) {
        service.activateClient(id);
    }
}
