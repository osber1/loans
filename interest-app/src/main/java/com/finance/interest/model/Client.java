package com.finance.interest.model;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    private String id;

    private String firstName;

    private String lastName;

    private Long personalCode;

    private Loan loan;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;
}
