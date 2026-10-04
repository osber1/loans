package io.osvaldas.messages;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RabbitMQMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    private final Duration confirmTimeout;

    public RabbitMQMessageProducer(RabbitTemplate rabbitTemplate,
                                   @Value("${rabbitmq.confirm-timeout:PT3S}") Duration confirmTimeout) {
        this.rabbitTemplate = rabbitTemplate;
        this.confirmTimeout = confirmTimeout;
    }

    public void publish(Object payload, String exchange, String routingKey) {
        log.info("Publishing message to exchange: {}, routingKey: {}", exchange, routingKey);
        CorrelationData correlationData = new CorrelationData();
        rabbitTemplate.convertAndSend(exchange, routingKey, payload, correlationData);
        awaitConfirmation(correlationData, exchange, routingKey);
        log.info("Published message to exchange: {}, routingKey: {}", exchange, routingKey);
    }

    private void awaitConfirmation(CorrelationData correlationData, String exchange, String routingKey) {
        try {
            CorrelationData.Confirm confirm = correlationData.getFuture().get(confirmTimeout.toMillis(), MILLISECONDS);
            if (!confirm.ack()) {
                throw new AmqpException("Broker rejected the message for exchange %s: %s".formatted(exchange, confirm.reason()));
            }
            if (correlationData.getReturned() != null) {
                throw new AmqpException("Message could not be routed from exchange %s with routing key %s".formatted(exchange, routingKey));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AmqpException("Interrupted while waiting for the broker to confirm the message", e);
        } catch (ExecutionException | TimeoutException e) {
            throw new AmqpException("No broker confirmation for the message to exchange %s".formatted(exchange), e);
        }
    }
}
