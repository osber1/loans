package io.osvaldas.risk.repositories.risk;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskValidationTarget {

    private BigDecimal loanAmount;

    private String clientId;

    private long loansTakenToday;

    private ZonedDateTime requestedAt;

}
