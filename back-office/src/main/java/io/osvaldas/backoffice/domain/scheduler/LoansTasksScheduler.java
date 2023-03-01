package io.osvaldas.backoffice.domain.scheduler;

import static io.osvaldas.api.loans.Status.NOT_EVALUATED;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.osvaldas.backoffice.domain.loans.LoanService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@RequiredArgsConstructor
public class LoansTasksScheduler {

    private final LoanService loanService;

    @Scheduled(cron = "* */10 * * * *")
    @SchedulerLock(name = "evaluateNotEvaluatedLoans", lockAtLeastFor = "PT30S", lockAtMostFor = "PT30S")
    public void evaluateNotEvaluatedLoans() {
        loanService.getLoansByStatus(NOT_EVALUATED)
            .forEach(loan -> loanService.validate(loan, loan.getClient().getId()));
    }

}
