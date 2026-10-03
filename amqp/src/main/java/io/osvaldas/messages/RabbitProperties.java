package io.osvaldas.messages;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitProperties {

    @Valid
    @NotNull
    private Exchanges exchanges;

    @Valid
    @NotNull
    private Queues queues;

    @Valid
    @NotNull
    private RoutingKeys routingKeys;

    @Getter
    @Setter
    public static class Exchanges {

        @NotBlank
        private String internal;

    }

    @Getter
    @Setter
    public static class Queues {

        @NotBlank
        private String notification;

    }

    @Getter
    @Setter
    public static class RoutingKeys {

        @NotBlank
        private String internalNotification;

    }
}
