package com.finance.loans.infra.rest.dtos;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class LoanResponse {

    private long id;

    private BigDecimal amount;

    private BigDecimal interestRate;

    private Integer termInMonths;

    private ZonedDateTime returnDate;

    private Set<LoanPostponeResponse> loanPostpones;
}