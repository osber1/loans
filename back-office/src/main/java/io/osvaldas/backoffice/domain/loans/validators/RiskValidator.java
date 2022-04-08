package io.osvaldas.backoffice.domain.loans.validators;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import io.osvaldas.backoffice.domain.loans.rules.TimeAndAmountValidationRule;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RiskValidator implements Validator {

    private final List<TimeAndAmountValidationRule> timeAndAmountValidationRule;

    @Override
    public void validate(BigDecimal clientAmount) {
        timeAndAmountValidationRule.forEach(v -> v.validate(clientAmount));
    }
}
