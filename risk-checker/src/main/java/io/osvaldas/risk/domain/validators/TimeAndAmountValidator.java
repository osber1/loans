package io.osvaldas.risk.domain.validators;

import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS;
import static io.osvaldas.api.util.ExceptionMessages.RISK_TOO_HIGH;

import java.math.BigDecimal;

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
public class TimeAndAmountValidator implements ValidationRule {

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    @Override
    public void validate(RiskValidationTarget target) {
        checkTimeAndAmount(target.getLoanAmount());
        checkIfAmountIsNotToHigh(target.getLoanAmount());
    }

    private void checkTimeAndAmount(BigDecimal amount) {
        int currentHour = timeUtils.getHourOfDay();
        if (config.getForbiddenHourFrom() <= currentHour && currentHour <= config.getForbiddenHourTo() && amount.compareTo(config.getMaxAmount()) == 0) {
            throw new TimeException(RISK_TOO_HIGH);
        }
    }

    private void checkIfAmountIsNotToHigh(BigDecimal clientAmount) {
        if (clientAmount.compareTo(config.getMaxAmount()) > 0) {
            throw new AmountException(AMOUNT_EXCEEDS);
        }
    }

}
