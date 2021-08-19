package com.finance.interest.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IpWipeOutScheduler {

    private final RedisTemplate<String, Integer> redisTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    private void wipeOutIps() {
        for (String key : redisTemplate.keys("*")) {
            redisTemplate.delete(key);
        }
    }
}
