package io.osvaldas.api.risk.validation;

public record RiskValidationResponse(boolean success, RiskRejectionReason reason, String message) {

    public static final String PASSED_MESSAGE = "Risk validation passed.";

    public static RiskValidationResponse passed() {
        return new RiskValidationResponse(true, null, PASSED_MESSAGE);
    }

    public static RiskValidationResponse rejected(RiskRejectionReason reason, String message) {
        return new RiskValidationResponse(false, reason, message);
    }

}
