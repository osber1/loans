package io.osvaldas.backoffice.repositories.entities;

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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

    public void setNewInterestRate(BigDecimal interestRate, BigDecimal interestIncrementFactor) {
        BigDecimal newInterestRate = interestRate.multiply(interestIncrementFactor).setScale(2, HALF_UP);
        setInterestRate(newInterestRate);
    }

    public void setNewReturnDay(ZonedDateTime returnDate, int postponeDays) {
        ZonedDateTime newReturnDay = returnDate.plusDays(postponeDays);
        setReturnDate(newReturnDay);
    }
}
