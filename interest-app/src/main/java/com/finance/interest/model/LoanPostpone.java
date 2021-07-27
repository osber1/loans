package com.finance.interest.model;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.EqualsAndHashCode;
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
    private long id;

    private ZonedDateTime newReturnDate;

    private BigDecimal newInterestRate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoanPostpone that = (LoanPostpone) o;
        return id == that.id && Objects.equals(newReturnDate, that.newReturnDate) && Objects.equals(newInterestRate, that.newInterestRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, newReturnDate, newInterestRate);
    }
}

