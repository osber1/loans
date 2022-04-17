package io.osvaldas.fraud.domain.validation;

import io.osvaldas.fraud.repositories.risk.RiskValidationObject;

public interface Validator {

    void validate(RiskValidationObject riskValidationObject);
}
