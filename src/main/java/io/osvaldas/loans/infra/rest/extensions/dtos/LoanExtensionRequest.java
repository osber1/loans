package io.osvaldas.loans.infra.rest.extensions.dtos;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LoanExtensionRequest {

    private ZonedDateTime newReturnDate;

    private BigDecimal newInterestRate;
}
