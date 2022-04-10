package io.osvaldas.messages;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class RabbitMQMessageProducer {

    private final AmqpTemplate amqpTemplate;

    public void publish(Object payload, String exchange, String routingKey) {
        log.info("Publishing message to exchange: {}, routingKey: {}", exchange, routingKey);
        amqpTemplate.convertAndSend(exchange, routingKey, payload);
        log.info("Published message to exchange: {}, routingKey: {}", exchange, routingKey);
    }
}
