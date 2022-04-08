package io.osvaldas.notifications.infra.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "email")
public class PropertiesConfig {

    private String senderAddress;

    private String subject;

    private String activationLink;
}