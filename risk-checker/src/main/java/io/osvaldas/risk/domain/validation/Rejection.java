package io.osvaldas.risk.domain.validation;

import io.osvaldas.api.risk.validation.RiskRejectionReason;

public record Rejection(RiskRejectionReason reason, String message) {

}
