package io.osvaldas.notifications.infra.configuration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;

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

    @NotNull
    private String host;

    @NotNull
    private int port;
}
