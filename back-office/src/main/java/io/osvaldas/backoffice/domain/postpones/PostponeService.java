package io.osvaldas.backoffice.domain.postpones;

import static io.osvaldas.api.loans.Status.OPEN;
import static java.lang.String.format;
import static java.util.Optional.of;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${exceptionMessages.loanNotOpen:}")
    private String loanNotOpen;

    @Transactional
    public LoanPostpone postponeLoan(long id) {
        log.info("Postponing loan: {}", id);
        Loan loan = getOpenLoan(id);
        loan.postponeLoan(config);
        return loanService.save(loan).getLastLoanPostpone();
    }

    private Loan getOpenLoan(long id) {
        return of(loanService.getLoan(id))
            .filter(savedLoan -> OPEN == savedLoan.getStatus())
            .orElseThrow(() -> new BadRequestException(format(loanNotOpen, id)));
    }
}
