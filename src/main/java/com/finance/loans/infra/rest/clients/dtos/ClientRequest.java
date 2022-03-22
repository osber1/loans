package com.finance.loans.infra.rest.clients.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ClientRequest {

    @NotBlank(message = "First name must be not empty.")
    private String firstName;

    @NotBlank(message = "Last name must be not empty.")
    private String lastName;

    @Email
    @NotBlank(message = "Email must be not empty.")
    private String email;

    @NotBlank(message = "Phone number must be not empty.")
    @Size(message = "Phone number must be 12 digits length and start with \"+\".", min = 12, max = 12)
    private String phoneNumber;

    @NotBlank(message = "Personal code must be not empty.")
    @Size(message = "Personal code must be 11 digits length.", min = 11, max = 11)
    private String personalCode;
}