package io.osvaldas.api.risk.validation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiskValidationResponse {

    private boolean success;

    private String message;
}
