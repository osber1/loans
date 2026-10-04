package io.osvaldas.backoffice.domain.scheduler

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.NOT_EVALUATED

import java.time.Duration

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.risk.validation.RiskRejectionReason
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.loans.LoanService
import io.osvaldas.backoffice.domain.loans.RiskCheckerUnavailableException
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared
import spock.lang.Subject

class LoansTasksSchedulerSpec extends AbstractSpec {

    static final Duration MIN_AGE = Duration.ofMinutes(5)

    static final int BATCH_SIZE = 2

    @Shared
    Loan loan = buildLoan(100.0).tap {
        it.client = buildClient(CLIENT_ID, [it] as Set, ACTIVE)
    }

    LoanService loanService = Mock()

    @Subject
    LoansTasksScheduler scheduler = new LoansTasksScheduler(loanService, MIN_AGE, BATCH_SIZE)

    void 'should evaluate #invocations times when there are #result.size() loans'() {
        given:
            loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 0, BATCH_SIZE) >> result
            loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, loan.id, BATCH_SIZE) >> []
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
            loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 0, BATCH_SIZE) >> [failingLoan, secondLoan]
            loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 2, BATCH_SIZE) >> [thirdLoan]
            loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 3, BATCH_SIZE) >> []
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
                new ValidationRuleException(RiskRejectionReason.LOAN_LIMIT_EXCEEDS, LOAN_LIMIT_EXCEEDS),
                new IllegalStateException('boom'),
            ]
    }

    void 'should fetch loans in batches after the last evaluated loan until there are no more'() {
        given:
            List<Loan> loans = (1..5).collect { buildLoanWithClient(it) }
        when:
            scheduler.evaluateNotEvaluatedLoans()
        then:
            1 * loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 0, BATCH_SIZE) >> loans[0..1]
            1 * loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 2, BATCH_SIZE) >> loans[2..3]
            1 * loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 4, BATCH_SIZE) >> [loans[4]]
            1 * loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 5, BATCH_SIZE) >> []
        and:
            5 * loanService.validate(_ as Loan, CLIENT_ID)
    }

    void 'should stop the run when the risk checker is unavailable'() {
        given:
            Loan firstLoan = buildLoanWithClient(1)
            Loan secondLoan = buildLoanWithClient(2)
            loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 0, BATCH_SIZE) >> [firstLoan, secondLoan]
        when:
            scheduler.evaluateNotEvaluatedLoans()
        then:
            1 * loanService.validate(firstLoan, CLIENT_ID) >> { throw new RiskCheckerUnavailableException(null) }
            0 * loanService.validate(secondLoan, CLIENT_ID)
            0 * loanService.getLoansByStatusOlderThan(NOT_EVALUATED, MIN_AGE, 2, _)
        and:
            noExceptionThrown()
    }

    private Loan buildLoanWithClient(long loanId) {
        buildLoan(100.0, NOT_EVALUATED).tap {
            id = loanId
            client = buildClient(CLIENT_ID, [it] as Set, ACTIVE)
        }
    }

}
