package io.osvaldas.backoffice.domain.scheduler;

import static io.osvaldas.api.loans.Status.NOT_EVALUATED;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.osvaldas.backoffice.domain.loans.LoanService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "scheduler.evaluateNotEvaluatedLoans", name = "enabled", havingValue = "true")
public class LoansTasksScheduler {

    private final LoanService loanService;

    @Scheduled(cron = "${scheduler.evaluateNotEvaluatedLoans.cron: 0 0 0 0 * *}")
    @SchedulerLock(name = "evaluateNotEvaluatedLoans", lockAtLeastFor = "PT5S", lockAtMostFor = "PT30S")
    public void evaluateNotEvaluatedLoans() {
        loanService.getLoansByStatus(NOT_EVALUATED)
            .forEach(loan -> loanService.validate(loan, loan.getClient().getId()));
    }

}
