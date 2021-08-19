package com.finance.interest.util;

import javax.transaction.Transactional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.finance.interest.configuration.PropertiesConfig;
import com.finance.interest.interfaces.IpValidationRule;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IpValidator implements IpValidationRule {

    private static final String TOO_MANY_REQUESTS = "Too many requests from the same ip per day.";

    private final PropertiesConfig config;

    private final RedisTemplate<String, Integer> redisTemplate;

    @Override
    @Transactional
    public void validate(String ip) {
        checkIpAddress(ip, config.getRequestsFromSameIpLimit());
    }

    private void checkIpAddress(String ipAddress, int requestsFromSameIpLimit) {
        Integer ipTimesUsed = redisTemplate.opsForValue().get(ipAddress);
        if (ipTimesUsed != null) {
            if (ipTimesUsed >= requestsFromSameIpLimit) {
                throw new IpException(TOO_MANY_REQUESTS);
            } else {
                redisTemplate.opsForValue().set(ipAddress, ipTimesUsed + 1);
            }
        } else {
            redisTemplate.opsForValue().set(ipAddress, 1);
        }
    }

    public static class IpException extends ValidationRuleException {

        public IpException(String message) {
            super(message);
        }
    }
}
