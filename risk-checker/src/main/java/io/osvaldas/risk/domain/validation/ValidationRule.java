package io.osvaldas.risk.domain.validation;

import io.osvaldas.risk.repositories.risk.RiskValidationTarget;

public interface ValidationRule {

    void validate(RiskValidationTarget target);
}
