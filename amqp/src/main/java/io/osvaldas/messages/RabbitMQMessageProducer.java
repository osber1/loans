package io.osvaldas.messages;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class RabbitMQMessageProducer {

    private final AmqpTemplate amqpTemplate;

    public void publish(Object payload, String exchange, String routingKey) {
        amqpTemplate.convertAndSend(exchange, routingKey, payload);
    }
}