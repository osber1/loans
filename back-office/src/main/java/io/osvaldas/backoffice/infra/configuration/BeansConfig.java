package io.osvaldas.backoffice.infra.configuration;

import static java.time.Duration.ofMinutes;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;
import static tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static tools.jackson.databind.cfg.DateTimeFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS;
import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_WITH_ZONE_ID;

import javax.sql.DataSource;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.backoffice.domain.loans.RiskCheckerClient;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import tools.jackson.databind.json.JsonMapper;

@EnableCaching
@EnableScheduling
@Configuration
@EnableFeignClients(clients = RiskCheckerClient.class)
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class BeansConfig {

    public static final String LOAN_RESPONSE_CACHE = "LoanResponse";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        JsonMapper mapper = configureCacheMapper(JsonMapper.builder()).build();
        RedisCacheConfiguration loanResponseCache = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(ofMinutes(60))
            .disableCachingNullValues()
            .serializeValuesWith(fromSerializer(new JacksonJsonRedisSerializer<>(mapper, LoanResponse.class)));
        RedisCacheWriter cacheWriter = RedisCacheWriter.create(connectionFactory, writer -> writer.immediateWrites());

        return RedisCacheManager.builder(cacheWriter)
            .withCacheConfiguration(LOAN_RESPONSE_CACHE, loanResponseCache)
            .disableCreateOnMissingCache()
            .transactionAware()
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

    private static JsonMapper.Builder configureCacheMapper(JsonMapper.Builder builder) {
        return builder
            .disable(FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .disable(ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .enable(WRITE_DATES_WITH_ZONE_ID);
    }

}
