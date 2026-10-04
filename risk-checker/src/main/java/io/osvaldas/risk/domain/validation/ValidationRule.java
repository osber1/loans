package io.osvaldas.risk.domain.validation;

import java.util.Optional;

import io.osvaldas.api.risk.validation.RiskValidationRequest;

public interface ValidationRule {

    Optional<Rejection> check(RiskValidationRequest request);
}
