package io.osvaldas.backoffice.repositories.entities;

import static io.osvaldas.api.loans.Status.PENDING;
import static java.util.Comparator.comparing;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;
import static javax.persistence.EnumType.STRING;

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

import io.osvaldas.api.loans.Status;
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig;
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

    @Enumerated(STRING)
    private Status status = PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @OneToMany(cascade = ALL)
    private Set<LoanPostpone> loanPostpones = new HashSet<>();

    @JoinColumn(name = "client_id")
    @ManyToOne(cascade = { PERSIST, MERGE, DETACH, REFRESH })
    private Client client;

    public void addLoanPostpone(LoanPostpone loanPostpone) {
        loanPostpones.add(loanPostpone);
    }

    public void setInterestAndReturnDate(BigDecimal interestRate, ZonedDateTime currentDateTime) {
        setInterestRate(interestRate);
        setReturnDate(currentDateTime.plusMonths(getTermInMonths()));
    }

    public void postponeLoan(PropertiesConfig config) {
        getLoanPostpones().stream()
            .max(comparing(LoanPostpone::getReturnDate))
            .ifPresentOrElse(postpone -> setNewInterestAndReturnDay(new LoanPostpone(), postpone.getInterestRate(), postpone.getReturnDate(), config),
                () -> setNewInterestAndReturnDay(new LoanPostpone(), getInterestRate(), getReturnDate(), config));
    }

    private void setNewInterestAndReturnDay(LoanPostpone loanPostpone, BigDecimal interestRate, ZonedDateTime returnDate, PropertiesConfig config) {
        loanPostpone.setNewInterestRate(interestRate, config.getInterestIncrementFactor());
        loanPostpone.setNewReturnDay(returnDate, config.getPostponeDays());
        addLoanPostpone(loanPostpone);
    }
}
