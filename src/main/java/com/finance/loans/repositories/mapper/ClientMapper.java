package com.finance.loans.repositories.mapper;

import org.mapstruct.Mapper;

import com.finance.loans.infra.rest.dtos.ClientRequest;
import com.finance.loans.infra.rest.dtos.ClientResponse;
import com.finance.loans.repositories.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse clientToDTO(Client client);

    Client clientToEntity(ClientRequest clientDto);
}
