package com.finance.loans.infra.rest.dtos;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class ClientResponse {

    private String id;

    private String firstName;

    private String lastName;

    private String personalCode;

    private LoanResponse loan;

    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;
}