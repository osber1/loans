package io.osvaldas.loans.domain.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate

import com.jupitertools.springtestredis.RedisTestContainer

import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@RedisTestContainer
class IpWipeOutSchedulerSpec extends Specification {

    @Shared
    String key = 'key'

    @Autowired
    RedisTemplate<String, Integer> redisTemplate

    @Autowired
    IpWipeOutScheduler ipWipeOutScheduler

    void 'should wipe out all ips from redis storage'() {
        given:
            redisTemplate.opsForValue().set(key, 1)
        when:
            ipWipeOutScheduler.wipeOutIps()
        then:
            redisTemplate.opsForValue().get(key) == null
    }
}
