package io.osvaldas.api.loans;

import static io.osvaldas.api.loans.Status.NOT_EVALUATED;
import static java.math.BigDecimal.ZERO;
import static java.time.ZonedDateTime.now;

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

    public LoanResponse() {
        this(0L, ZERO, ZERO, 0, NOT_EVALUATED, now(), now(), Set.of());
    }

}
