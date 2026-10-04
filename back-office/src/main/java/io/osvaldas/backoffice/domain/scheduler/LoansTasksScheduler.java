package io.osvaldas.backoffice.domain.scheduler;

import static io.osvaldas.api.loans.Status.NOT_EVALUATED;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException;
import io.osvaldas.backoffice.domain.loans.LoanService;
import io.osvaldas.backoffice.domain.loans.RiskCheckerUnavailableException;
import io.osvaldas.backoffice.repositories.entities.Loan;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "scheduler.evaluateNotEvaluatedLoans", name = "enabled", havingValue = "true")
public class LoansTasksScheduler {

    private final LoanService loanService;

    private final Duration minAge;

    private final int batchSize;

    public LoansTasksScheduler(LoanService loanService,
                               @Value("${scheduler.evaluateNotEvaluatedLoans.minAge:PT5M}") Duration minAge,
                               @Value("${scheduler.evaluateNotEvaluatedLoans.batchSize:100}") int batchSize) {
        this.loanService = loanService;
        this.minAge = minAge;
        this.batchSize = batchSize;
    }

    @Scheduled(cron = "${scheduler.evaluateNotEvaluatedLoans.cron:0 */10 * * * *}")
    @SchedulerLock(name = "evaluateNotEvaluatedLoans", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30M")
    public void evaluateNotEvaluatedLoans() {
        try {
            evaluateAllBatches();
        } catch (RiskCheckerUnavailableException e) {
            log.warn("Risk checker is unavailable, stopping the scheduled evaluation");
        }
    }

    private void evaluateAllBatches() {
        List<Loan> batch = nextBatch(0);
        while (!batch.isEmpty()) {
            batch.forEach(this::evaluate);
            batch = nextBatch(batch.getLast().getId());
        }
    }

    private List<Loan> nextBatch(long afterId) {
        return loanService.getLoansByStatusOlderThan(NOT_EVALUATED, minAge, afterId, batchSize);
    }

    private void evaluate(Loan loan) {
        try {
            loanService.validate(loan, loan.getClient().getId());
        } catch (ValidationRuleException e) {
            log.warn("Loan {} rejected during scheduled evaluation: {}", loan.getId(), e.getMessage());
        } catch (RiskCheckerUnavailableException e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Failed to evaluate loan {}", loan.getId(), e);
        }
    }

}
