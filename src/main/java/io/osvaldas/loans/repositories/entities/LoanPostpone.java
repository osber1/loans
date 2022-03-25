package io.osvaldas.loans.repositories.entities;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class LoanPostpone {

    @Id
    @GeneratedValue(generator = "POSTPONE_SEQ", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "POSTPONE_SEQ", sequenceName = "POSTPONE_SEQ", allocationSize = 1)
    private long id;

    private ZonedDateTime newReturnDate;

    private BigDecimal newInterestRate;
}