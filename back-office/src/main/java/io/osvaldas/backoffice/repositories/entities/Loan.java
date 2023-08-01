package io.osvaldas.backoffice.repositories.entities;

import static io.osvaldas.api.loans.Status.PENDING;
import static java.util.Comparator.comparing;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import io.osvaldas.api.loans.Status;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Audited
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

    @Enumerated(STRING)
    private Status status = PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "loan", cascade = ALL, fetch = LAZY)
    private Set<LoanPostpone> loanPostpones = new HashSet<>();

    @JoinColumn(name = "client_id")
    @ManyToOne(cascade = { DETACH, MERGE, PERSIST, REFRESH })
    private Client client;

    public void addLoanPostpone(LoanPostpone loanPostpone) {
        loanPostpones.add(loanPostpone);
    }

    public LoanPostpone getLastLoanPostpone() {
        return getLoanPostpones().stream()
            .max(comparing(LoanPostpone::getReturnDate))
            .orElse(null);
    }

    public void setInterestAndReturnDate(BigDecimal interestRate, ZonedDateTime currentDateTime) {
        setInterestRate(interestRate);
        setReturnDate(currentDateTime.plusMonths(getTermInMonths()));
    }

    public void postponeLoan(int postponeDays, BigDecimal factor) {
        getLoanPostpones().stream()
            .max(comparing(LoanPostpone::getReturnDate))
            .ifPresentOrElse(postpone -> setNewInterestAndReturnDay(postpone.getInterestRate(), postpone.getReturnDate(), postponeDays, factor),
                () -> setNewInterestAndReturnDay(getInterestRate(), getReturnDate(), postponeDays, factor));
    }

    private void setNewInterestAndReturnDay(BigDecimal interestRate, ZonedDateTime returnDate, int postponeDays, BigDecimal factor) {
        LoanPostpone loanPostpone = new LoanPostpone();
        loanPostpone.incrementAndSetInterestRate(interestRate, factor);
        loanPostpone.incrementAndSetReturnDay(returnDate, postponeDays);
        addLoanPostpone(loanPostpone);
        loanPostpone.setLoan(this);
    }
}
