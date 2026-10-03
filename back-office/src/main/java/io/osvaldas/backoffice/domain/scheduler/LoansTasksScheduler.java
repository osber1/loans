package io.osvaldas.backoffice.domain.scheduler;

import static io.osvaldas.api.loans.Status.NOT_EVALUATED;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException;
import io.osvaldas.backoffice.domain.loans.LoanService;
import io.osvaldas.backoffice.repositories.entities.Loan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduler.evaluateNotEvaluatedLoans", name = "enabled", havingValue = "true")
public class LoansTasksScheduler {

    private final LoanService loanService;

    /**
     * Re-evaluates loans whose risk validation did not complete (e.g. risk-checker was unavailable).
     * Runs every 10 minutes by default (override with {@code scheduler.evaluateNotEvaluatedLoans.cron}).
     * {@code lockAtMostFor} is generous because every loan triggers a remote risk-checker call.
     */
    @Scheduled(cron = "${scheduler.evaluateNotEvaluatedLoans.cron:0 */10 * * * *}")
    @SchedulerLock(name = "evaluateNotEvaluatedLoans", lockAtLeastFor = "PT5S", lockAtMostFor = "PT10M")
    public void evaluateNotEvaluatedLoans() {
        loanService.getLoansByStatus(NOT_EVALUATED)
            .forEach(this::evaluate);
    }

    private void evaluate(Loan loan) {
        try {
            loanService.validate(loan, loan.getClient().getId());
        } catch (ValidationRuleException e) {
            log.warn("Loan {} rejected during scheduled evaluation: {}", loan.getId(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("Failed to evaluate loan {}", loan.getId(), e);
        }
    }

}
