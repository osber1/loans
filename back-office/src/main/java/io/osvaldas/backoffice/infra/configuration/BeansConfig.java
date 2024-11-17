package io.osvaldas.backoffice.infra.configuration;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS;
import static java.time.Duration.ofMinutes;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class BeansConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        JsonMapper mapper = JsonMapper.builder()
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

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }

    @Bean
    public ThreadPoolTaskScheduler cronJobThreadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(3);
        threadPoolTaskScheduler.setThreadNamePrefix("cronJobThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

}
