package io.osvaldas.fraud.repositories.risk;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskValidationTarget {

    private BigDecimal loanAmount;

    private String clientId;

}
