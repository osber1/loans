package io.osvaldas.fraud.domain.validation;

import io.osvaldas.fraud.repositories.risk.RiskValidationTarget;

public interface ValidationRule {

    void validate(RiskValidationTarget target);
}
