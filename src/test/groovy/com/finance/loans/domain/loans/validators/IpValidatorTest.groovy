package com.finance.loans.domain.loans.validators

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

import com.finance.loans.domain.loans.validators.IpValidator.IpException
import com.finance.loans.infra.configuration.PropertiesConfig

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class IpValidatorTest extends Specification {

    @Shared
    String IP_ADDRESS_TO_CHECK = '0.0.0.0.0.0.1'

    private PropertiesConfig config = Stub()

    private RedisTemplate<String, Integer> redisTemplate = Stub()

    private ValueOperations valueOperations = Stub()

    @Subject
    private IpValidator ipValidator = new IpValidator(config, redisTemplate)

    void 'should validate when ip limit is not exceeded and log is new'() {
        given:
            redisTemplate.opsForValue() >> valueOperations
            valueOperations.get(_ as String) >> null
            config.requestsFromSameIpLimit >> 5
        when:
            ipValidator.validate(IP_ADDRESS_TO_CHECK)
        then:
            notThrown(IpException)
    }

    void 'should validate when ip limit is not exceeded and log is not new'() {
        given:
            redisTemplate.opsForValue() >> valueOperations
            valueOperations.get(_ as String) >> 2
            config.requestsFromSameIpLimit >> 5
        when:
            ipValidator.validate(IP_ADDRESS_TO_CHECK)
        then:
            notThrown(IpException)
    }

    void 'should throw exception when ip limit is exceeded'() {
        given:
            redisTemplate.opsForValue() >> valueOperations
            valueOperations.get(_ as String) >> 5
            config.requestsFromSameIpLimit >> 5
        when:
            ipValidator.validate(IP_ADDRESS_TO_CHECK)
        then:
            IpException e = thrown()
        and:
            e.message == 'Too many requests from the same ip per day.'
    }
}
