package io.osvaldas.loans.repositories.entities;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
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
    @GeneratedValue(generator = "LOANS_SEQ", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "LOANS_SEQ", sequenceName = "LOANS_SEQ", allocationSize = 1)
    private long id;

    @NotNull
    private BigDecimal amount;

    private BigDecimal interestRate;

    @NotNull
    private Integer termInMonths;

    private ZonedDateTime returnDate;

    @OneToMany(cascade = CascadeType.ALL)
    private Set<LoanPostpone> loanPostpones = new HashSet<>();

    public void setNewLoanInterestAndReturnDate(BigDecimal interestRate, ZonedDateTime currentDateTime) {
        setInterestRate(interestRate);
        setReturnDate(currentDateTime.plusMonths(getTermInMonths()));
    }
}