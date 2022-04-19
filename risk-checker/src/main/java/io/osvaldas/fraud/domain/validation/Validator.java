package io.osvaldas.fraud.domain.validation;

import io.osvaldas.fraud.repositories.risk.RiskValidationTarget;

public interface Validator {

    void validate(RiskValidationTarget riskValidationTarget);
}
