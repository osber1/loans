package io.osvaldas.backoffice.repositories.mapper;

import java.util.Collection;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.osvaldas.api.clients.ClientRegisterRequest;
import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.clients.ClientUpdateRequest;
import io.osvaldas.backoffice.repositories.entities.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse map(Client client);

    Client map(ClientRegisterRequest clientDto);

    Collection<ClientResponse> map(Collection<Client> all);

    /**
     * Maps only the fields a client is allowed to change, plus id and version used to locate the client and detect
     * concurrent modifications. Status and personal code are deliberately not mapped.
     */
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "personalCode", ignore = true)
    @Mapping(target = "loans", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Client mapToEntity(ClientUpdateRequest clientDto);

}
