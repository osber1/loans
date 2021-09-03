package com.finance.loans.domain.loans.validators;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finance.loans.domain.loans.rules.TimeAndAmountValidationRule;
import com.finance.loans.domain.loans.rules.Validator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskValidator implements Validator {

    private final List<TimeAndAmountValidationRule> timeAndAmountValidationRule;

    @Override
    public void validate(BigDecimal clientAmount) {
        timeAndAmountValidationRule.forEach(v -> v.validate(clientAmount));
    }
}
