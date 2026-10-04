package io.osvaldas.notifications

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.ApplicationContext
import org.springframework.test.util.ReflectionTestUtils

import groovy.json.JsonSlurper
import io.osvaldas.api.email.EmailMessage
import spock.lang.Shared
import spock.util.concurrent.PollingConditions

class IntegrationSpec extends AbstractIntegrationSpec {

    @Shared
    String clientId = 'clientId'

    @Shared
    String token = 'activation-token'

    @Shared
    String fullName = 'Name Surname'

    @Shared
    HttpClient httpClient = HttpClient.newHttpClient()

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    AmqpTemplate amqpTemplate

    @Autowired
    @Qualifier('rabbitListenerContainerFactory')
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory

    PollingConditions conditions = new PollingConditions(timeout: 10)

    void setup() {
        send(mailhogRequest('/api/v1/messages').DELETE().build())
    }

    void 'should send email when message is consumed'() {
        given:
            EmailMessage message = new EmailMessage(clientId, fullName, receiverEmail, token)
        when:
            amqpTemplate.convertAndSend(exchangeName, routingKeys, message)
        then:
            conditions.eventually {
                List<Map> messages = mailhogMessages()
                assert messages.size() == 1
                Map headers = messages[0].Content.Headers
                assert headers.From == [emailSender]
                assert headers.To == [receiverEmail]
                assert headers.Subject == [emailSubject]
                String body = messages[0].Content.Body
                assert body.contains("Hi ${fullName},")
                assert body.contains("/api/v1/clients/${clientId}/activation?token=${token}")
                assert body.contains('text/plain')
                assert body.contains('text/html')
            }
    }

    void 'should dead-letter a message that keeps failing instead of retrying it forever'() {
        given:
            EmailMessage message = new EmailMessage(clientId, fullName, 'not an email address', token)
        when:
            amqpTemplate.convertAndSend(exchangeName, routingKeys, message)
        then:
            conditions.eventually {
                assert amqpTemplate.receive(deadLetterQueueName) != null
            }
        and:
            amqpTemplate.receive(queueName) == null
            mailhogMessages().isEmpty()
    }

    void 'should use a single json converting rabbit template and listener factory'() {
        expect:
            applicationContext.getBeansOfType(AmqpTemplate).size() == 1
            (amqpTemplate as RabbitTemplate).messageConverter instanceof JacksonJsonMessageConverter
            ReflectionTestUtils.getField(rabbitListenerContainerFactory, 'messageConverter')
                instanceof JacksonJsonMessageConverter
    }

    private static HttpRequest.Builder mailhogRequest(String path) {
        HttpRequest.newBuilder(URI.create(mailhogApiUrl(path)))
    }

    private List<Map> mailhogMessages() {
        HttpResponse<String> response = send(mailhogRequest('/api/v2/messages').GET().build())
        (new JsonSlurper().parseText(response.body()) as Map).items as List<Map>
    }

    private HttpResponse<String> send(HttpRequest request) {
        httpClient.send(request, HttpResponse.BodyHandlers.ofString())
    }

}
