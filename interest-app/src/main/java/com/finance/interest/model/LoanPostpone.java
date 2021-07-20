package com.finance.interest.model;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class LoanPostpone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private ZonedDateTime newReturnDate;

    private double newInterestRate;
}

