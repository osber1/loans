package io.osvaldas.api.risk.validation;

/**
 * Machine-readable cause of a rejected risk validation, so callers do not have to match on message text.
 */
public enum RiskRejectionReason {
    AMOUNT_EXCEEDS,
    FORBIDDEN_TIME,
    LOAN_LIMIT_EXCEEDS
}
