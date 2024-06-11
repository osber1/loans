package io.osvaldas.api.loans;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LoanRequest(@Min(1) @NotNull(message = "Amount must be not empty.") BigDecimal amount,
                          @Min(1) @NotNull(message = "Term in months must be not empty.") Integer termInMonths) {

}
