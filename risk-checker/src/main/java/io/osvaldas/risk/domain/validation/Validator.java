package io.osvaldas.risk.domain.validation;

import io.osvaldas.risk.repositories.risk.RiskValidationTarget;

public interface Validator {

    void validate(RiskValidationTarget riskValidationTarget);
}
