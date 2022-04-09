package io.osvaldas.backoffice.infra.rest.postpones.dtos;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LoanPostponeResponse {

    private long id;

    private ZonedDateTime returnDate;

    private BigDecimal interestRate;
}
