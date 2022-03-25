package io.osvaldas.loans.infra.rest.extensions.dtos;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LoanExtensionResponse {

    private long id;

    private ZonedDateTime newReturnDate;

    private BigDecimal newInterestRate;
}
