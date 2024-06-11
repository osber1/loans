package io.osvaldas.api.clients;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientUpdateRequest(@NotBlank(message = "Id must be not empty.") String id,
                                  @NotBlank(message = "First name must be not empty.") String firstName,
                                  @NotBlank(message = "Last name must be not empty.") String lastName,
                                  Status status,
                                  @Email @NotBlank(message = "Email must be not empty.") String email,
                                  @NotBlank(message = "Phone number must be not empty.")
                                  @Size(message = "Phone number must be 11 digits length and start with country code.", min = 11, max = 11) String phoneNumber,
                                  @Pattern(regexp = "^\\d+$", message = "Personal code must contain only digits.") @NotBlank(message = "Personal code must be not empty.") @Size(message = "Personal code must be 11 digits length.", min = 11, max = 11) String personalCode,
                                  @NotNull(message = "Version must be not empty.") Long version) {

}
