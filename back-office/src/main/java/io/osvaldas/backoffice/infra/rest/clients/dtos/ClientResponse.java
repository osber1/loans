package io.osvaldas.backoffice.infra.rest.clients.dtos;

import java.time.ZonedDateTime;

import io.osvaldas.backoffice.repositories.entities.Status;
import lombok.Data;

@Data
public class ClientResponse {

    private String id;

    private String firstName;

    private String lastName;

    private String personalCode;

    private Status status;

    private String email;

    private String phoneNumber;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;
}