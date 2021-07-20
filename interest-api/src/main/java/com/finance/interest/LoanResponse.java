package com.finance.interest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class LoanResponse {

    private int id;

    private BigDecimal amount;

    private double interestRate;

    private Integer termInMonths;

    private ZonedDateTime returnDate;

    private Set<LoanPostponeResponse> loanPostpones;
}