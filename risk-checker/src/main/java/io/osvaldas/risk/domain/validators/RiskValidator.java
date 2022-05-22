package io.osvaldas.risk.domain.validators;

import java.util.List;

import org.springframework.stereotype.Service;

import io.osvaldas.risk.domain.validation.ValidationRule;
import io.osvaldas.risk.domain.validation.Validator;
import io.osvaldas.risk.repositories.risk.RiskValidationTarget;
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
