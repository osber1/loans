package io.osvaldas.backoffice.infra.configuration;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.json.JsonMapper.builder;
import static java.time.Duration.ofMinutes;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class BeansConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        JsonMapper mapper = builder()
            .addModule(new JavaTimeModule())
            .activateDefaultTyping(new ObjectMapper().getPolymorphicTypeValidator(), NON_FINAL, PROPERTY)
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(WRITE_DATE_KEYS_AS_TIMESTAMPS)
            .build();

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ofMinutes(60))
            .disableCachingNullValues()
            .serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)));
    }

}
