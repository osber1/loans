package com.finance.interest.model;

import java.time.ZonedDateTime;
import java.util.Comparator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Client {

    private String id;

    private String firstName;

    private String lastName;

    private Long personalCode;

    private Loan loan;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    public static Client buildClientResponse(ClientDAO savedClient) {
        return Client.builder()
            .id(savedClient.getId())
            .firstName(savedClient.getFirstName())
            .lastName(savedClient.getLastName())
            .personalCode(savedClient.getPersonalCode())
            .loan(savedClient.getLoans().stream().max(Comparator.comparing(Loan::getId)).get())
            .createdAt(savedClient.getCreatedAt())
            .updatedAt(savedClient.getUpdatedAt()).build();
    }
}
