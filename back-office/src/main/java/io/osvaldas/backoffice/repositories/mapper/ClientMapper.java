package io.osvaldas.backoffice.repositories.mapper;

import java.util.Collection;

import org.mapstruct.Mapper;

import io.osvaldas.api.clients.ClientRegisterRequest;
import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.clients.ClientUpdateRequest;
import io.osvaldas.backoffice.repositories.entities.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse map(Client client);

    Client map(ClientRegisterRequest clientDto);

    Collection<ClientResponse> map(Collection<Client> all);

    Client mapToEntity(ClientUpdateRequest clientDto);

}
