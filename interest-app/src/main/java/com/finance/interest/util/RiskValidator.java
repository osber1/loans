package com.finance.interest.util;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finance.interest.interfaces.IpValidationRule;
import com.finance.interest.interfaces.TimeAndAmountValidationRule;
import com.finance.interest.interfaces.Validator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskValidator implements Validator {

    private final List<IpValidationRule> ipValidationRule;

    private final List<TimeAndAmountValidationRule> timeAndAmountValidationRule;

    @Override
    public void validate(String ip) {
        ipValidationRule.forEach(v -> v.validate(ip));
    }

    @Override
    public void validate(BigDecimal clientAmount) {
        timeAndAmountValidationRule.forEach(v -> v.validate(clientAmount));
    }
}
