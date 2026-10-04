package io.osvaldas.messages

import java.time.Duration

import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.Message
import org.springframework.amqp.core.ReturnedMessage
import org.springframework.amqp.rabbit.connection.CorrelationData
import org.springframework.amqp.rabbit.core.RabbitTemplate

import spock.lang.Specification
import spock.lang.Subject

class RabbitMQMessageProducerSpec extends Specification {

    static final String EXCHANGE = 'internal.exchange'

    static final String ROUTING_KEY = 'internal.notification.routing-key'

    static final String PAYLOAD = 'payload'

    RabbitTemplate rabbitTemplate = Mock()

    @Subject
    RabbitMQMessageProducer producer = new RabbitMQMessageProducer(rabbitTemplate, Duration.ofMillis(100))

    void 'should publish the message when the broker confirms and routes it'() {
        when:
            producer.publish(PAYLOAD, EXCHANGE, ROUTING_KEY)
        then:
            1 * rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, PAYLOAD, _ as CorrelationData) >> {
                String exchange, String routingKey, Object payload, CorrelationData correlation ->
                    correlation.future.complete(new CorrelationData.Confirm(true, null))
            }
        and:
            noExceptionThrown()
    }

    void 'should fail when the broker rejects the message'() {
        when:
            producer.publish(PAYLOAD, EXCHANGE, ROUTING_KEY)
        then:
            1 * rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, PAYLOAD, _ as CorrelationData) >> {
                String exchange, String routingKey, Object payload, CorrelationData correlation ->
                    correlation.future.complete(new CorrelationData.Confirm(false, 'no exchange'))
            }
        and:
            AmqpException e = thrown()
            e.message.contains('no exchange')
    }

    void 'should fail when the message is confirmed but returned as unroutable'() {
        when:
            producer.publish(PAYLOAD, EXCHANGE, ROUTING_KEY)
        then:
            1 * rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, PAYLOAD, _ as CorrelationData) >> {
                String exchange, String routingKey, Object payload, CorrelationData correlation ->
                    correlation.returned = new ReturnedMessage(
                        new Message(new byte[0]), 312, 'NO_ROUTE', exchange, routingKey)
                    correlation.future.complete(new CorrelationData.Confirm(true, null))
            }
        and:
            AmqpException e = thrown()
            e.message.contains('could not be routed')
    }

    void 'should fail when the broker does not confirm in time'() {
        when:
            producer.publish(PAYLOAD, EXCHANGE, ROUTING_KEY)
        then:
            1 * rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, PAYLOAD, _ as CorrelationData)
        and:
            thrown(AmqpException)
    }

    void 'should fail when the channel closes before the confirmation arrives'() {
        when:
            producer.publish(PAYLOAD, EXCHANGE, ROUTING_KEY)
        then:
            1 * rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, PAYLOAD, _ as CorrelationData) >> {
                String exchange, String routingKey, Object payload, CorrelationData correlation ->
                    correlation.future.completeExceptionally(new IllegalStateException('channel closed'))
            }
        and:
            thrown(AmqpException)
    }

}
