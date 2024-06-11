package io.osvaldas.api.clients;

import io.osvaldas.api.annotations.MobilePhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientRegisterRequest(@NotBlank(message = "First name must be not empty.") String firstName,
                                    @NotBlank(message = "Last name must be not empty.") String lastName,
                                    @Email @NotBlank(message = "Email must be not empty.") String email,
                                    @MobilePhone @NotBlank(message = "Phone number must be not empty.") String phoneNumber,
                                    @Pattern(regexp = "^\\d+$", message = "Personal code must contain only digits.")
                                    @NotBlank(message = "Personal code must be not empty.")
                                    @Size(message = "Personal code must be 11 digits length.", min = 11, max = 11) String personalCode) {

}
