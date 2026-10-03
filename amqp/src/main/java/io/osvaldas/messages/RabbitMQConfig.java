package io.osvaldas.messages;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers a JSON {@link MessageConverter}.
 *
 * <p>Spring Boot applies it to the auto-configured {@code RabbitTemplate} and to the default
 * {@code rabbitListenerContainerFactory} used by {@code @RabbitListener}.
 */
@Configuration(proxyBeanMethods = false)
public class RabbitMQConfig {

    @Bean
    public MessageConverter jacksonConverter() {
        return new JacksonJsonMessageConverter();
    }
}
