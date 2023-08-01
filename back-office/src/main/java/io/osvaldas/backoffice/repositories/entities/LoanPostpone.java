package io.osvaldas.backoffice.repositories.entities;

import static java.math.RoundingMode.HALF_UP;
import static javax.persistence.CascadeType.DETACH;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REFRESH;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Audited
public class LoanPostpone {

    @Id
    @GeneratedValue(generator = "POSTPONE_SEQ", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "POSTPONE_SEQ", sequenceName = "POSTPONE_SEQ", allocationSize = 1)
    private long id;

    private ZonedDateTime returnDate;

    private BigDecimal interestRate;

    @JoinColumn(name = "loan_id")
    @ManyToOne(cascade = { DETACH, MERGE, PERSIST, REFRESH })
    private Loan loan;

    public void incrementAndSetInterestRate(BigDecimal interestRate, BigDecimal interestIncrementFactor) {
        BigDecimal newInterestRate = interestRate.multiply(interestIncrementFactor).setScale(2, HALF_UP);
        setInterestRate(newInterestRate);
    }

    public void incrementAndSetReturnDay(ZonedDateTime returnDate, int postponeDays) {
        ZonedDateTime newReturnDay = returnDate.plusDays(postponeDays);
        setReturnDate(newReturnDay);
    }
}
