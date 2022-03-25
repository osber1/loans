package io.osvaldas.loans.repositories.mapper;

import java.util.Collection;

import org.mapstruct.Mapper;

import io.osvaldas.loans.infra.rest.clients.dtos.ClientRequest;
import io.osvaldas.loans.infra.rest.clients.dtos.ClientResponse;
import io.osvaldas.loans.repositories.entities.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse clientToDTO(Client client);

    Client clientToEntity(ClientRequest clientDto);

    Collection<ClientResponse> clientsToDTOs(Collection<Client> all);
}
