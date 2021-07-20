package com.finance.interest;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class LoanPostponeResponse {

    private int id;

    private ZonedDateTime newReturnDate;

    private double newInterestRate;
}
