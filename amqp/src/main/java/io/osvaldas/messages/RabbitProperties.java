package io.osvaldas.messages;

import javax.validation.constraints.NotBlank;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "rabbitmq")
public class RabbitProperties {

    private Exchanges exchanges;

    private Queues queues;

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
