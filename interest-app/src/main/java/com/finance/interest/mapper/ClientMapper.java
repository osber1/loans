package com.finance.interest.mapper;

import org.mapstruct.Mapper;

import com.finance.interest.ClientRequest;
import com.finance.interest.ClientResponse;
import com.finance.interest.model.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientResponse clientToDTO(Client client);

    Client clientToEntity(ClientRequest clientDto);
}
