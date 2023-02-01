package io.osvaldas.api.loans;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
