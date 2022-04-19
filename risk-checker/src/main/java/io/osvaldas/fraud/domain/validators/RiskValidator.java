package io.osvaldas.fraud.domain.validators;

import java.util.List;

import org.springframework.stereotype.Service;

import io.osvaldas.fraud.domain.validation.ValidationRule;
import io.osvaldas.fraud.domain.validation.Validator;
import io.osvaldas.fraud.repositories.risk.RiskValidationTarget;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RiskValidator implements Validator {

    private final List<ValidationRule> rules;

    @Override
    public void validate(RiskValidationTarget target) {
        rules.forEach(rule -> rule.validate(target));
    }
}
