package io.osvaldas.risk.domain.validators;

import static io.osvaldas.api.risk.validation.RiskRejectionReason.AMOUNT_EXCEEDS;
import static io.osvaldas.api.risk.validation.RiskRejectionReason.FORBIDDEN_TIME;
import static io.osvaldas.api.util.ExceptionMessages.RISK_TOO_HIGH;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.util.ExceptionMessages;
import io.osvaldas.api.util.TimeUtils;
import io.osvaldas.risk.domain.validation.Rejection;
import io.osvaldas.risk.domain.validation.ValidationRule;
import io.osvaldas.risk.infra.configuration.PropertiesConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(TimeAndAmountValidator.ORDER)
public class TimeAndAmountValidator implements ValidationRule {

    static final int ORDER = 1;

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    @Override
    public Optional<Rejection> check(RiskValidationRequest request) {
        return checkTimeAndAmount(request).or(() -> checkIfAmountIsNotToHigh(request.amount()));
    }

    private int requestHour(RiskValidationRequest request) {
        ZonedDateTime now = timeUtils.getCurrentDateTime();
        return Optional.ofNullable(request.requestedAt())
            .map(requestedAt -> requestedAt.withZoneSameInstant(now.getZone()))
            .orElse(now)
            .getHour();
    }

    private Optional<Rejection> checkTimeAndAmount(RiskValidationRequest request) {
        int from = config.getForbiddenHourFrom();
        int to = config.getForbiddenHourTo();
        if (isWithinWindow(requestHour(request), from, to) && request.amount().compareTo(config.getMaxAmount()) == 0) {
            return Optional.of(new Rejection(FORBIDDEN_TIME, RISK_TOO_HIGH.formatted(formatHour(from), formatHour(to))));
        }
        return Optional.empty();
    }

    private Optional<Rejection> checkIfAmountIsNotToHigh(BigDecimal clientAmount) {
        if (clientAmount.compareTo(config.getMaxAmount()) > 0) {
            return Optional.of(new Rejection(AMOUNT_EXCEEDS, ExceptionMessages.AMOUNT_EXCEEDS));
        }
        return Optional.empty();
    }

    static boolean isWithinWindow(int hour, int from, int to) {
        if (from <= to) {
            return from <= hour && hour < to;
        }
        return hour >= from || hour < to;
    }

    private static String formatHour(int hour) {
        return "%02d:00".formatted(hour);
    }

}
