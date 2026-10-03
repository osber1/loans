package io.osvaldas.api.risk.validation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RiskValidationRequest(@NotNull Long loanId, @NotEmpty String clientId, @NotNull @PositiveOrZero Long loansTakenToday) {

}
