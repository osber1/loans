package io.osvaldas.api.postpones;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record LoanPostponeResponse(long id, ZonedDateTime returnDate, BigDecimal interestRate) {

}
