package io.osvaldas.backoffice.domain.scheduler

import static io.osvaldas.api.loans.Status.NOT_EVALUATED

import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.loans.LoanService
import spock.lang.Subject

class LoansTasksSchedulerSpec extends AbstractSpec {

    LoanService loanService = Mock()

    @Subject
    LoansTasksScheduler scheduler = new LoansTasksScheduler(loanService)

    void 'should evaluate #invocations times when there are #result.size() loans'() {
        given:
            loanService.getLoansByStatus(NOT_EVALUATED) >> result
        when:
            scheduler.evaluateNotEvaluatedLoans()
        then:
            invocations * loanService.validate(loan, loan.client.id)
        where:
            result || invocations
            [loan] || 1
            []     || 0
    }

}