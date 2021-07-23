package com.finance.interest.util;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finance.interest.interfaces.ValidationRule;
import com.finance.interest.interfaces.Validator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiskValidator implements Validator {

    private final List<ValidationRule> validationRule;

    @Override
    public void validate(String ip, BigDecimal clientAmount) {
        validationRule.forEach(v -> v.validate(ip, clientAmount));
    }
}
