package io.osvaldas.backoffice.domain.postpones;

import static io.osvaldas.api.loans.Status.OPEN;
import static io.osvaldas.api.util.ExceptionMessages.LOAN_NOT_OPEN;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.api.exceptions.BadRequestException;
import io.osvaldas.backoffice.domain.loans.LoanService;
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig;
import io.osvaldas.backoffice.repositories.entities.Loan;
import io.osvaldas.backoffice.repositories.entities.LoanPostpone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostponeService {

    private final LoanService loanService;

    private final PropertiesConfig config;

    @Transactional
    public LoanPostpone postponeLoan(long id) {
        log.info("Postponing loan: {}", id);
        Loan loan = getOpenLoan(id);
        loan.postponeLoan(config.getPostponeDays(), config.getInterestIncrementFactor());
        return loanService.save(loan).getLastLoanPostpone();
    }

    private Loan getOpenLoan(long id) {
        return Optional.of(loanService.getLoan(id))
            .filter(savedLoan -> OPEN == savedLoan.getStatus())
            .orElseThrow(() -> new BadRequestException(LOAN_NOT_OPEN.formatted(id)));
    }
}
