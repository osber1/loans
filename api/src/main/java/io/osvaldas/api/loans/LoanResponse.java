package io.osvaldas.api.loans;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import io.osvaldas.api.postpones.LoanPostponeResponse;

public record LoanResponse(long id,
                           BigDecimal amount,
                           BigDecimal interestRate,
                           Integer termInMonths,
                           Status status,
                           ZonedDateTime returnDate,
                           ZonedDateTime createdAt,
                           Set<LoanPostponeResponse> loanPostpones) {

}
