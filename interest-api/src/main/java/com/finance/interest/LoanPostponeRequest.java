package com.finance.interest;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LoanPostponeRequest {

    private ZonedDateTime newReturnDate;

    private double newInterestRate;
}
