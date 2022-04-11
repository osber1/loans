package io.osvaldas.backoffice.domain.util;

import static java.util.stream.Stream.of;

import java.util.Objects;

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
        of(redisTemplate.keys("*"))
            .filter(Objects::nonNull)
            .forEach(redisTemplate::delete);
    }
}
