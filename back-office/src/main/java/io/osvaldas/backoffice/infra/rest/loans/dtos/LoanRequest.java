package io.osvaldas.backoffice.infra.rest.loans.dtos;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class LoanRequest {

    @Min(value = 1)
    @NotNull(message = "Amount must be not empty.")
    private BigDecimal amount;

    @Min(value = 1)
    @NotNull(message = "Term in months must be not empty.")
    private Integer termInMonths;
}
