package io.osvaldas.api.clients;

import io.osvaldas.api.annotations.MobilePhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterRequest {

    @NotBlank(message = "First name must be not empty.")
    private String firstName;

    @NotBlank(message = "Last name must be not empty.")
    private String lastName;

    @Email
    @NotBlank(message = "Email must be not empty.")
    private String email;

    @NotBlank(message = "Phone number must be not empty.")
    @MobilePhone
    private String phoneNumber;

    @Pattern(regexp = "^\\d+$", message = "Personal code must contain only digits.")
    @NotBlank(message = "Personal code must be not empty.")
    @Size(message = "Personal code must be 11 digits length.", min = 11, max = 11)
    private String personalCode;
}
