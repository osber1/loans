package io.osvaldas.fraud.domain.validators;

import java.util.List;

import org.springframework.stereotype.Service;

import io.osvaldas.fraud.domain.rules.LoanLimitValidationRule;
import io.osvaldas.fraud.domain.rules.TimeAndAmountValidationRule;
import io.osvaldas.fraud.domain.validation.Validator;
import io.osvaldas.fraud.repositories.risk.RiskValidationObject;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RiskValidator implements Validator {

    private final List<TimeAndAmountValidationRule> timeAndAmountValidationRule;

    private final List<LoanLimitValidationRule> loanLimitValidationRule;

    @Override
    public void validate(RiskValidationObject object) {
        timeAndAmountValidationRule.forEach(rule -> rule.validate(object.getLoanAmount()));
        loanLimitValidationRule.forEach(rule -> rule.validate(object.getClientId()));

    }
}
