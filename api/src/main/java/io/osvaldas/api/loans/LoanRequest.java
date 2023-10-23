package io.osvaldas.api.loans;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    @Min(1)
    @NotNull(message = "Amount must be not empty.")
    private BigDecimal amount;

    @Min(1)
    @NotNull(message = "Term in months must be not empty.")
    private Integer termInMonths;
}
