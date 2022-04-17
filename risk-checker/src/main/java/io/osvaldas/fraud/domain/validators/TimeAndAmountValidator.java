package io.osvaldas.fraud.domain.validators;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException.AmountException;
import io.osvaldas.api.exceptions.ValidationRuleException.TimeException;
import io.osvaldas.api.util.TimeUtils;
import io.osvaldas.fraud.domain.rules.TimeAndAmountValidationRule;
import io.osvaldas.fraud.infra.configuration.PropertiesConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimeAndAmountValidator implements TimeAndAmountValidationRule {

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    @Value("${exceptionMessages.amountExceedsMessage:}")
    private String amountExceedsMessage;

    @Value("${exceptionMessages.riskMessage:}")
    private String riskMessage;

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

}
