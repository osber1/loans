package com.finance.interest.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;
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
    private long id;

    @NotNull
    private BigDecimal amount;

    private BigDecimal interestRate;

    @NotNull
    private Integer termInMonths;

    private ZonedDateTime returnDate;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<LoanPostpone> loanPostpones;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loan loan = (Loan) o;
        return id == loan.id && Objects.equals(amount, loan.amount) && Objects.equals(interestRate, loan.interestRate) && Objects.equals(termInMonths,
            loan.termInMonths) && Objects.equals(returnDate, loan.returnDate) && Objects.equals(loanPostpones, loan.loanPostpones);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, interestRate, termInMonths, returnDate, loanPostpones);
    }
}