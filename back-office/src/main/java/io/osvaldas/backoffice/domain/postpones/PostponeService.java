package io.osvaldas.backoffice.domain.postpones;

import static java.util.Comparator.comparing;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.backoffice.domain.loans.LoanService;
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig;
import io.osvaldas.backoffice.repositories.entities.Loan;
import io.osvaldas.backoffice.repositories.entities.LoanPostpone;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class PostponeService {

    private final LoanService loanService;

    private final PropertiesConfig config;

    @Transactional
    public LoanPostpone postponeLoan(long id) {
        log.info("Postponing loan: {}", id);
        Loan loan = loanService.getLoan(id);
        loan.postponeLoan(config);
        return loanService.save(loan)
            .getLoanPostpones().stream()
            .max(comparing(LoanPostpone::getReturnDate))
            .orElse(null);
    }
}
