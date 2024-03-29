package io.osvaldas.notifications.infra.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "email")
public class PropertiesConfig {

    @NotBlank
    private String senderAddress;

    @NotBlank
    private String subject;

    @NotBlank
    private String activationLink;

}
