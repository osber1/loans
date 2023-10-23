package io.osvaldas.api.risk.validation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskValidationRequest {

    @NotNull
    private long loanId;

    @NotEmpty
    private String clientId;
}
