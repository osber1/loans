package io.osvaldas.backoffice.domain.postpones

import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.loans.LoanService
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig
import io.osvaldas.backoffice.repositories.entities.LoanPostpone
import spock.lang.Subject

class PostponeServiceSpec extends AbstractSpec {

    LoanService loanService = Stub()

    PropertiesConfig config = Stub {
        interestIncrementFactor >> 1.5
        postponeDays >> 7
    }

    @Subject
    PostponeService postponeService = new PostponeService(loanService, config)

    void 'should postpone loan when it is first postpone'() {
        given:
            loanService.getLoan(loanId) >> loan
            loanService.save(loan) >> addPostponeToLoan(loan, firstPostpone)
        when:
            LoanPostpone loanPostpone = postponeService.postponeLoan(loanId)
        then:
            firstPostpone == loanPostpone
    }

    void 'should postpone loan when it is not first postpone'() {
        given:
            loanService.getLoan(loanId) >> addPostponeToLoan(loan, firstPostpone)
            loanService.save(loan) >> addPostponeToLoan(loan, secondPostpone)
        when:
            LoanPostpone loanPostpone = postponeService.postponeLoan(loanId)
        then:
            secondPostpone == loanPostpone
    }

}
