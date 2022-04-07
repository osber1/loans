package io.osvaldas.loans.repositories.mapper;

import java.util.Collection;

import org.mapstruct.Mapper;

import io.osvaldas.loans.infra.rest.clients.dtos.ClientRegisterRequest;
import io.osvaldas.loans.infra.rest.clients.dtos.ClientResponse;
import io.osvaldas.loans.infra.rest.clients.dtos.ClientUpdateRequest;
import io.osvaldas.loans.repositories.entities.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse clientToDTO(Client client);

    Client clientRegisterToEntity(ClientRegisterRequest clientDto);

    Client clientUpdateToEntity(ClientUpdateRequest clientDto);

    Collection<ClientResponse> clientsToDTOs(Collection<Client> all);
}
