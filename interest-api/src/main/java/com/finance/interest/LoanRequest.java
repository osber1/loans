package com.finance.interest;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class LoanRequest {

    @NotNull(message = "Amount must be not empty.")
    private BigDecimal amount;

    @NotNull(message = "Term in months must be not empty.")
    private Integer termInMonths;
}