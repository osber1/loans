package io.osvaldas.api.clients;

import java.time.ZonedDateTime;

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

    private long version;
}
