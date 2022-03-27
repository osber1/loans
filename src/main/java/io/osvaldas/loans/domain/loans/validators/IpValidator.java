package io.osvaldas.loans.domain.loans.validators;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import javax.transaction.Transactional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.osvaldas.loans.domain.loans.rules.IpValidationRule;
import io.osvaldas.loans.infra.configuration.PropertiesConfig;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class IpValidator implements IpValidationRule {

    private static final String TOO_MANY_REQUESTS = "Too many requests from the same ip per day.";

    private final PropertiesConfig config;

    private final RedisTemplate<String, Integer> redisTemplate;

    @Override
    @Transactional
    public void validate(String ip) {
        int requestsFromSameIpLimit = config.getRequestsFromSameIpLimit();
        ofNullable(redisTemplate.opsForValue().get(ip))
            .ifPresentOrElse(s -> checkIfIpLimitIsNotExceeded(ip, requestsFromSameIpLimit, s),
            () -> redisTemplate.opsForValue().set(ip, 1));
    }

    private void checkIfIpLimitIsNotExceeded(String ipAddress, int requestsFromSameIpLimit, Integer ipTimesUsed) {
        of(ipTimesUsed)
            .filter(timesUsed -> timesUsed < requestsFromSameIpLimit)
            .ifPresentOrElse(timesUsed -> redisTemplate.opsForValue().set(ipAddress, timesUsed + 1),
                () -> {
                    throw new IpException(TOO_MANY_REQUESTS);
                });
    }

    public static class IpException extends ValidationRuleException {

        public IpException(String message) {
            super(message);
        }
    }
}
