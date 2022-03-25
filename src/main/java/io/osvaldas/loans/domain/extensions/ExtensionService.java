package io.osvaldas.loans.domain.extensions;

import static java.util.Comparator.comparing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.loans.domain.loans.LoanService;
import io.osvaldas.loans.infra.configuration.PropertiesConfig;
import io.osvaldas.loans.repositories.entities.Loan;
import io.osvaldas.loans.repositories.entities.LoanPostpone;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ExtensionService {

    private static final int NUMBERS_AFTER_COMMA = 2;

    private final LoanService loanService;

    private final PropertiesConfig config;

    @Transactional
    public LoanPostpone postponeLoan(long id) {
        Loan loan = loanService.get(id);
        LoanPostpone loanPostpone = buildLoanPostpone(loan);
        loan.getLoanPostpones().add(loanPostpone);
        loan.setReturnDate(loanPostpone.getNewReturnDate());
        return loanService.save(loan).getLoanPostpones().stream().max(comparing(LoanPostpone::getNewReturnDate)).orElse(null);
    }

    private LoanPostpone buildLoanPostpone(Loan loan) {
//        of(loan)
//            .map(Loan::getLoanPostpones);
        LoanPostpone loanPostpone = new LoanPostpone();
        ZonedDateTime newReturnDate;
        BigDecimal newInterestRate;
        if (loan.getLoanPostpones().isEmpty()) {
            newReturnDate = loan.getReturnDate();
            newInterestRate = loan.getInterestRate();
        } else {
            LoanPostpone latestPostpone = loan.getLoanPostpones().stream().max(comparing(LoanPostpone::getNewReturnDate)).orElse(null);
            newReturnDate = latestPostpone.getNewReturnDate();
            newInterestRate = latestPostpone.getNewInterestRate();
        }
        loanPostpone.setNewInterestRate(calculateNewInterestRate(newInterestRate).setScale(NUMBERS_AFTER_COMMA, RoundingMode.HALF_UP));
        loanPostpone.setNewReturnDate(calculateNewReturnDate(newReturnDate));
        return loanPostpone;
    }

    private BigDecimal calculateNewInterestRate(BigDecimal newInterestRate) {
        return newInterestRate.multiply(config.getInterestIncrementFactor());
    }

    private ZonedDateTime calculateNewReturnDate(ZonedDateTime newReturnDate) {
        return newReturnDate.plusDays(config.getPostponeDays());
    }
}
