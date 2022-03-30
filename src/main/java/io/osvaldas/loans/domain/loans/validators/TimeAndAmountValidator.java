package io.osvaldas.loans.domain.loans.validators;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.osvaldas.loans.domain.loans.rules.TimeAndAmountValidationRule;
import io.osvaldas.loans.domain.util.TimeUtils;
import io.osvaldas.loans.infra.configuration.PropertiesConfig;

@Component
public class TimeAndAmountValidator implements TimeAndAmountValidationRule {

    private final String riskMessage;

    private final String amountExceedsMessage;

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    public TimeAndAmountValidator(@Value("${exceptionMessages.riskMessage:}") String riskMessage, @Value("${exceptionMessages.amountExceedsMessage:}") String amountExceedsMessage,
                                  PropertiesConfig config, TimeUtils timeUtils) {
        this.riskMessage = riskMessage;
        this.amountExceedsMessage = amountExceedsMessage;
        this.config = config;
        this.timeUtils = timeUtils;
    }

    @Override
    public void validate(BigDecimal clientAmount) {
        checkTimeAndAmount(clientAmount);
        checkIfAmountIsNotToHigh(clientAmount);
    }

    private void checkTimeAndAmount(BigDecimal amount) {
        int currentHour = timeUtils.getHourOfDay();
        if (config.getForbiddenHourFrom() <= currentHour && currentHour <= config.getForbiddenHourTo() && amount.compareTo(config.getMaxAmount()) == 0) {
            throw new TimeException(riskMessage);
        }
    }

    private void checkIfAmountIsNotToHigh(BigDecimal clientAmount) {
        if (clientAmount.compareTo(config.getMaxAmount()) > 0) {
            throw new AmountException(amountExceedsMessage);
        }
    }

    public static class AmountException extends ValidationRuleException {

        public AmountException(String message) {
            super(message);
        }
    }

    public static class TimeException extends ValidationRuleException {

        public TimeException(String message) {
            super(message);
        }
    }
}
