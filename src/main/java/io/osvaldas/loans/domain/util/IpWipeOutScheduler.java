package io.osvaldas.loans.domain.util;

import static java.util.Objects.requireNonNull;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class IpWipeOutScheduler {

    private final RedisTemplate<String, Integer> redisTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    private void wipeOutIps() {
        for (String key : requireNonNull(redisTemplate.keys("*"))) {
            redisTemplate.delete(key);
        }
    }
}
