package io.osvaldas.backoffice.infra.configuration;

import static java.time.Clock.systemUTC;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class BeansConfig {

    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public Clock clock() {
        return systemUTC();
    }
}
