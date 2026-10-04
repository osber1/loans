package io.osvaldas.api.clients;

import io.osvaldas.api.annotations.MobilePhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClientUpdateRequest(@NotBlank(message = "Id must be not empty.") String id,
                                  @NotBlank(message = "First name must be not empty.") String firstName,
                                  @NotBlank(message = "Last name must be not empty.") String lastName,
                                  @Email @NotBlank(message = "Email must be not empty.") String email,
                                  @NotBlank(message = "Phone number must be not empty.")
                                  @MobilePhone(message = "Phone number must be 11 digits length and start with country code.") String phoneNumber,
                                  @NotNull(message = "Version must be not empty.") Long version) {

}
