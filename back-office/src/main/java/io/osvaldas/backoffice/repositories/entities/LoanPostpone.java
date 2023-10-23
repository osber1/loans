package io.osvaldas.backoffice.repositories.entities;

import static jakarta.persistence.CascadeType.DETACH;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REFRESH;
import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.hibernate.envers.Audited;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
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
