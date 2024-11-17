package io.osvaldas.api.risk.validation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RiskValidationRequest(@NotNull Long loanId, @NotEmpty String clientId) {

    public RiskValidationRequest() {
        this(0L, "");
    }

}
