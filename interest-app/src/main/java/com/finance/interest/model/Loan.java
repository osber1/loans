package com.finance.interest.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private BigDecimal amount;

    private Double interestRate;

    @NotNull
    private Integer termInMonths;

    private ZonedDateTime returnDate;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<LoanPostpone> loanPostpones;
}