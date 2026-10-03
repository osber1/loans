package io.osvaldas.risk.domain.validators;

import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS;
import static io.osvaldas.api.util.ExceptionMessages.RISK_TOO_HIGH_BETWEEN_HOURS;

import java.math.BigDecimal;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException.AmountException;
import io.osvaldas.api.exceptions.ValidationRuleException.TimeException;
import io.osvaldas.api.util.TimeUtils;
import io.osvaldas.risk.domain.validation.ValidationRule;
import io.osvaldas.risk.infra.configuration.PropertiesConfig;
import io.osvaldas.risk.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(TimeAndAmountValidator.ORDER)
public class TimeAndAmountValidator implements ValidationRule {

    static final int ORDER = 1;

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    @Override
    public void validate(RiskValidationTarget target) {
        checkTimeAndAmount(target.getLoanAmount());
        checkIfAmountIsNotToHigh(target.getLoanAmount());
    }

    private void checkTimeAndAmount(BigDecimal amount) {
        int from = config.getForbiddenHourFrom();
        int to = config.getForbiddenHourTo();
        if (isWithinWindow(timeUtils.getHourOfDay(), from, to) && amount.compareTo(config.getMaxAmount()) == 0) {
            throw new TimeException(RISK_TOO_HIGH_BETWEEN_HOURS.formatted(formatHour(from), formatHour(to)));
        }
    }

    private void checkIfAmountIsNotToHigh(BigDecimal clientAmount) {
        if (clientAmount.compareTo(config.getMaxAmount()) > 0) {
            throw new AmountException(AMOUNT_EXCEEDS);
        }
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
