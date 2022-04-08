package io.osvaldas.backoffice.repositories.mapper;

import java.util.Collection;

import org.mapstruct.Mapper;

import io.osvaldas.backoffice.infra.rest.clients.dtos.ClientRegisterRequest;
import io.osvaldas.backoffice.infra.rest.clients.dtos.ClientResponse;
import io.osvaldas.backoffice.infra.rest.clients.dtos.ClientUpdateRequest;
import io.osvaldas.backoffice.repositories.entities.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse clientToDTO(Client client);

    Client clientRegisterToEntity(ClientRegisterRequest clientDto);

    Client clientUpdateToEntity(ClientUpdateRequest clientDto);

    Collection<ClientResponse> clientsToDTOs(Collection<Client> all);
}
