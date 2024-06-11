package io.osvaldas.api.clients;

import java.time.ZonedDateTime;

public record ClientResponse(String id,
                             String firstName,
                             String lastName,
                             String personalCode,
                             Status status,
                             String email,
                             String phoneNumber,
                             ZonedDateTime createdAt,
                             ZonedDateTime updatedAt,
                             long version) {

}
