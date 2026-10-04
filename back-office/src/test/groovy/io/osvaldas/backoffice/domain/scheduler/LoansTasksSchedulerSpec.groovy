package io.osvaldas.backoffice.domain.scheduler

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.NOT_EVALUATED

import java.time.Duration

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.loans.LoanService
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared
import spock.lang.Subject

class LoansTasksSchedulerSpec extends AbstractSpec {

    static final Duration MIN_AGE = Duration.ofMinutes(5)

    @Shared
    Loan loan = buildLoan(100.0).tap {
        it.client = buildClient(CLIENT_ID, [it] as Set, ACTIVE)
    }

    LoanService loanService = Mock()

    @Subject
    LoansTasksScheduler scheduler = new LoansTasksScheduler(loanService, MIN_AGE)

    void 'should evaluate #invocations times when there are #result.size() loans'() {
        given:
            loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE) >> result
        when:
            scheduler.evaluateNotEvaluatedLoans()
        then:
            invocations * loanService.validate(loan, loan.client.id)
        where:
            result || invocations
            [loan] || 1
            []     || 0
    }

    void 'should continue evaluating remaining loans when a loan fails with #exception.class.simpleName'() {
        given:
            Loan failingLoan = buildLoanWithClient(1)
            Loan secondLoan = buildLoanWithClient(2)
            Loan thirdLoan = buildLoanWithClient(3)
            loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE) >> [failingLoan, secondLoan, thirdLoan]
        when:
            scheduler.evaluateNotEvaluatedLoans()
        then:
            1 * loanService.validate(failingLoan, CLIENT_ID) >> { throw exception }
            1 * loanService.validate(secondLoan, CLIENT_ID) >> { throw new IllegalStateException('unavailable') }
            1 * loanService.validate(thirdLoan, CLIENT_ID)
        and:
            noExceptionThrown()
        where:
            exception << [
                new ValidationRuleException.LoanLimitException(LOAN_LIMIT_EXCEEDS),
                new IllegalStateException('boom'),
            ]
    }

    private Loan buildLoanWithClient(long loanId) {
        buildLoan(100.0, NOT_EVALUATED).tap {
            id = loanId
            client = buildClient(CLIENT_ID, [it] as Set, ACTIVE)
        }
    }

}
