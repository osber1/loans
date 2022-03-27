package io.osvaldas.loans.infra.rest.postpones.dtos;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LoanPostponeRequest {

    private ZonedDateTime newReturnDate;

    private BigDecimal newInterestRate;
}
