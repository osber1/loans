package io.osvaldas.api.loans;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import io.osvaldas.api.postpones.LoanPostponeResponse;
import lombok.Data;

@Data
public class LoanResponse {

    private long id;

    private BigDecimal amount;

    private BigDecimal interestRate;

    private Integer termInMonths;

    private Status status;

    private ZonedDateTime returnDate;

    private ZonedDateTime createdAt;

    private Set<LoanPostponeResponse> loanPostpones;
}
