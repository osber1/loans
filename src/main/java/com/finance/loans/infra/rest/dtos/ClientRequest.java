package com.finance.loans.infra.rest.dtos;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank(message = "First name must be not empty.")
    private String firstName;

    @NotBlank(message = "Last name must be not empty.")
    private String lastName;

    @NotBlank(message = "Personal code must be not empty.")
    @Size(message = "Personal code must be 11 digits length.", min = 11, max = 11)
    private String personalCode;

    @Valid
    @NotNull(message = "Loan must be not empty.")
    private LoanRequest loan;
}