package com.finance.interest.util;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.finance.interest.configuration.PropertiesConfig;
import com.finance.interest.exception.BadRequestException;
import com.finance.interest.interfaces.TimeUtils;
import com.finance.interest.interfaces.ValidationRule;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimeAndAmountValidator implements ValidationRule {

    private static final String RISK_MESSAGE = "Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!";

    private static final String AMOUNT_EXCEEDS = "The amount you are trying to borrow exceeds the max amount!";

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    @Override
    public void validate(String ip, BigDecimal clientAmount) {
        checkTimeAndAmount(clientAmount, config.getMaxAmount(), config.getForbiddenHourFrom(), config.getForbiddenHourTo());
        checkIfAmountIsNotToHigh(clientAmount, config.getMaxAmount());
    }

    private void checkTimeAndAmount(BigDecimal amount, BigDecimal maxAmount, int forbiddenHourFrom, int forbiddenHourTo) {
        int currentHour = timeUtils.getHourOfDay();
        if (forbiddenHourFrom <= currentHour && currentHour <= forbiddenHourTo && amount.compareTo(maxAmount) == 0) {
            throw new BadRequestException(RISK_MESSAGE);
        }
    }

    private void checkIfAmountIsNotToHigh(BigDecimal clientAmount, BigDecimal maxAmount) {
        if (clientAmount.compareTo(maxAmount) > 0) {
            throw new BadRequestException(AMOUNT_EXCEEDS);
        }
    }

}
