package io.osvaldas.loans.domain.loans.validators

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

import io.osvaldas.loans.domain.loans.validators.IpValidator.IpException
import io.osvaldas.loans.infra.configuration.PropertiesConfig
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class IpValidatorSpec extends Specification {

    @Shared
    String ipAddress = '0.0.0.0.0.0.1'

    PropertiesConfig config = Stub {
        requestsFromSameIpLimit >> 5
    }

    ValueOperations valueOperations = Mock()

    RedisTemplate<String, Integer> redisTemplate = Stub {
        opsForValue() >> valueOperations
    }

    @Subject
    private IpValidator ipValidator = new IpValidator(config, redisTemplate)

    void 'should validate when ip limit is not exceeded and log is new'() {
        when:
            ipValidator.validate(ipAddress)
        then:
            notThrown(IpException)
        and:
            1 * valueOperations.get(ipAddress) >> null

    }

    void 'should validate when ip limit is not exceeded and log is not new'() {
        when:
            ipValidator.validate(ipAddress)
        then:
            notThrown(IpException)
        and:
            1 * valueOperations.get(ipAddress) >> 2
    }

    void 'should throw exception when ip limit is exceeded'() {
        when:
            ipValidator.validate(ipAddress)
        then:
            IpException e = thrown()
            e.message == 'Too many requests from the same ip per day.'
        and:
            1 * valueOperations.get(ipAddress) >> 5
    }
}
