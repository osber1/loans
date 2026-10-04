package io.osvaldas.notifications.infra.configuration

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

import io.osvaldas.messages.RabbitProperties
import spock.lang.Specification

class PropertiesValidationSpec extends Specification {

    static final List<String> VALID_PROPERTIES = [
        'email.senderAddress=hello@osber.com',
        'email.subject=Confirm your email',
        'email.activationLink=http://localhost:8080/api/v1/clients/%s/activation?token=%s',
        'rabbitmq.exchanges.internal=internal.exchange',
        'rabbitmq.queues.notification=notification.queue',
        'rabbitmq.routing-keys.internal-notification=internal.notification.routing-key',
    ]

    ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(PropertiesConfiguration)

    void 'should bind valid properties'() {
        expect:
            runner.withPropertyValues(VALID_PROPERTIES as String[]).run { context ->
                assert !context.startupFailure
                assert context.getBean(PropertiesConfig).activationLink.contains('/clients/')
                assert context.getBean(RabbitProperties).queues.notification == 'notification.queue'
            }
    }

    void 'should fail to start when #property is missing'() {
        given:
            List<String> properties = VALID_PROPERTIES.findAll { !it.startsWith("${property}=") }
        expect:
            runner.withPropertyValues(properties as String[]).run { context ->
                assert context.startupFailure
            }
        where:
            property << VALID_PROPERTIES.collect { it.substring(0, it.indexOf('=')) }
    }

    void 'should fail to start when nested rabbitmq property is blank'() {
        expect:
            runner.withPropertyValues(VALID_PROPERTIES as String[])
                .withPropertyValues('rabbitmq.queues.notification= ')
                .run { context ->
                    assert context.startupFailure
                }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties([PropertiesConfig, RabbitProperties])
    static class PropertiesConfiguration {

    }

}
