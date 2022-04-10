package io.osvaldas.backoffice.domain.util;

import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class IpWipeOutScheduler {

    private final RedisTemplate<String, Integer> redisTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    void wipeOutIps() {
        Stream.of(redisTemplate.keys("*"))
            .filter(Objects::nonNull)
            .forEach(redisTemplate::delete);
    }
}
