package io.osvaldas.backoffice.domain.notifications

import static io.osvaldas.backoffice.infra.rest.AbstractControllerSpec.TestRabbitTopologyConfig.EXCHANGE
import static io.osvaldas.backoffice.infra.rest.AbstractControllerSpec.TestRabbitTopologyConfig.QUEUE
import static io.osvaldas.backoffice.infra.rest.AbstractControllerSpec.TestRabbitTopologyConfig.ROUTING_KEY

import org.springframework.amqp.core.Binding
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired

import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.NotificationOutbox

class NotificationOutboxRelayIntegrationSpec extends AbstractControllerSpec {

    @Autowired
    NotificationOutboxService outboxService

    @Autowired
    RabbitAdmin rabbitAdmin

    @Autowired
    RabbitTemplate rabbitTemplate

    void setup() {
        rabbitAdmin.purgeQueue(QUEUE)
    }

    void cleanup() {
        rabbitAdmin.initialize()
    }

    void 'should mark the message as published and deliver it to the queue when the broker routes it'() {
        given:
            NotificationOutbox row = savePendingMessage()
        when:
            int published = outboxService.relayPending()
        then:
            published == 1
            notificationOutboxRepository.findById(row.id).get().publishedAt != null
        and:
            objectMapper.readValue(rabbitTemplate.receive(QUEUE, 5000L).body, Map) ==
                [clientId: CLIENT_ID, fullName: NAME, email: CLIENT_EMAIL]
    }

    void 'should keep the message pending when the exchange has no binding to a queue'() {
        given:
            NotificationOutbox row = savePendingMessage()
            rabbitAdmin.removeBinding(new Binding(QUEUE, Binding.DestinationType.QUEUE, EXCHANGE, ROUTING_KEY, [:]))
        when:
            int published = outboxService.relayPending()
        then:
            published == 0
            notificationOutboxRepository.findById(row.id).get().publishedAt == null
    }

    void 'should keep the message pending when the exchange does not exist'() {
        given:
            NotificationOutbox row = savePendingMessage()
            rabbitAdmin.deleteExchange(EXCHANGE)
        when:
            int published = outboxService.relayPending()
        then:
            published == 0
            notificationOutboxRepository.findById(row.id).get().publishedAt == null
    }

    void 'should relay the message once the broker topology is back'() {
        given:
            NotificationOutbox row = savePendingMessage()
            rabbitAdmin.deleteExchange(EXCHANGE)
            outboxService.relayPending()
        when:
            rabbitAdmin.initialize()
            int published = outboxService.relayPending()
        then:
            published == 1
            notificationOutboxRepository.findById(row.id).get().publishedAt != null
    }

    private NotificationOutbox savePendingMessage() {
        notificationOutboxRepository.save(new NotificationOutbox(CLIENT_ID, NAME, CLIENT_EMAIL))
    }

}
