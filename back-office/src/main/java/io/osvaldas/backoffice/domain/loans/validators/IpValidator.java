package io.osvaldas.backoffice.domain.loans.validators;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import io.osvaldas.backoffice.domain.loans.rules.IpValidationRule;
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class IpValidator implements IpValidationRule {

    private final PropertiesConfig config;

    private final RedisTemplate<String, Integer> redisTemplate;

    @Value("${exceptionMessages.ipExceedsMessage:}")
    private String ipExceedsMessage;

    @Override
    @Transactional
    public void validate(String ip) {
        int requestsFromSameIpLimit = config.getRequestsFromSameIpLimit();
        ofNullable(redisTemplate.opsForValue().get(ip))
            .ifPresentOrElse(s -> checkIfIpLimitIsNotExceeded(ip, requestsFromSameIpLimit, s),
                () -> redisTemplate.opsForValue().set(ip, 1));
    }

    private void checkIfIpLimitIsNotExceeded(String ipAddress, int requestsFromSameIpLimit, Integer ipTimesUsed) {
        // redisTemplate.keys("*").forEach(redisTemplate::delete);
        of(ipTimesUsed)
            .filter(timesUsed -> timesUsed < requestsFromSameIpLimit)
            .ifPresentOrElse(timesUsed -> redisTemplate.opsForValue().set(ipAddress, timesUsed + 1), () -> {
                throw new IpException(ipExceedsMessage);
            });
    }

    public static class IpException extends ValidationRuleException {

        public IpException(String message) {
            super(message);
        }
    }
}
