package com.finance.interest;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LoanPostponeResponse {

    private long id;

    private ZonedDateTime newReturnDate;

    private BigDecimal newInterestRate;
}
