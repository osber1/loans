package io.osvaldas.backoffice.infra.configuration;

import static java.time.Duration.ofMinutes;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.cfg.DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_WITH_ZONE_ID;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import io.osvaldas.api.loans.LoanResponse;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class BeansConfig {

    public static final String LOAN_RESPONSE_CACHE = "LoanResponse";

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        GenericJacksonJsonRedisSerializer serializer = GenericJacksonJsonRedisSerializer.builder()
            .customize(BeansConfig::configureCacheMapper)
            .enableDefaultTyping(BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("io.osvaldas.")
                .allowIfSubType("java.math.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.util.")
                .build())
            .build();

        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ofMinutes(60))
            .disableCachingNullValues()
            .serializeValuesWith(fromSerializer(serializer));
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                          RedisCacheConfiguration cacheConfiguration) {
        JsonMapper mapper = configureCacheMapper(JsonMapper.builder()).build();
        RedisCacheWriter cacheWriter = RedisCacheWriter.create(connectionFactory, writer -> writer.immediateWrites());

        return RedisCacheManager.builder(cacheWriter)
            .cacheDefaults(cacheConfiguration)
            .withCacheConfiguration(LOAN_RESPONSE_CACHE, cacheConfiguration
                .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(mapper, LoanResponse.class))))
            .build();
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

    private static JsonMapper.Builder configureCacheMapper(JsonMapper.Builder builder) {
        return builder
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .disable(ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .enable(WRITE_DATES_WITH_ZONE_ID);
    }

}
