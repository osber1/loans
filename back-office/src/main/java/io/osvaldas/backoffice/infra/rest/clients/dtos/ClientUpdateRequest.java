package io.osvaldas.backoffice.infra.rest.clients.dtos;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.osvaldas.backoffice.repositories.entities.Status;
import lombok.Data;

@Data
public class ClientUpdateRequest {

    private String id;

    @NotBlank(message = "First name must be not empty.")
    private String firstName;

    @NotBlank(message = "Last name must be not empty.")
    private String lastName;

    private Status status;

    @Email
    @NotBlank(message = "Email must be not empty.")
    private String email;

    @NotBlank(message = "Phone number must be not empty.")
    @Size(message = "Phone number must be 12 digits length and start with \"+\".", min = 12, max = 12)
    private String phoneNumber;

    @Pattern(regexp = "^\\d+$", message = "Personal code must contain only digits.")
    @NotBlank(message = "Personal code must be not empty.")
    @Size(message = "Personal code must be 11 digits length.", min = 11, max = 11)
    private String personalCode;
}