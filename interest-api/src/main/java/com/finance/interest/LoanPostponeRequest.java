package com.finance.interest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LoanPostponeRequest {

    private ZonedDateTime newReturnDate;

    private BigDecimal newInterestRate;
}
