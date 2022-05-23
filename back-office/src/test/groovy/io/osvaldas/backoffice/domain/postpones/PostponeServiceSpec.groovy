package io.osvaldas.backoffice.domain.postpones

import static io.osvaldas.api.loans.Status.CLOSED

import io.osvaldas.api.exceptions.BadRequestException
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

    void setup() {
        postponeService.loanNotOpen = loanNotOpen
    }

    void 'should postpone loan when it is first postpone'() {
        given:
            loanService.getLoan(loanId) >> loan
            loanService.save(loan) >> loan
        when:
            LoanPostpone loanPostpone = postponeService.postponeLoan(loanId)
        then:
            with(loanPostpone) {
                returnDate == firstPostpone.returnDate
                interestRate == firstPostpone.interestRate
            }
        and:
            loan.loanPostpones.size() == 1
    }

    void 'should postpone loan when it is not first postpone'() {
        given:
            loan.loanPostpones = [firstPostpone] as Set
        and:
            loanService.getLoan(loanId) >> loan
            loanService.save(loan) >> loan
        when:
            LoanPostpone loanPostpone = postponeService.postponeLoan(loanId)
        then:
            with(loanPostpone) {
                returnDate == secondPostpone.returnDate
                interestRate == secondPostpone.interestRate
            }
        and:
            loan.loanPostpones.size() == 2
    }

    void 'should throw when trying to postpone loan which is not open'() {
        given:
            loanService.getLoan(loanId) >> buildLoan(100.0, CLOSED)
        when:
            postponeService.postponeLoan(loanId)
        then:
            BadRequestException e = thrown()
            e.message == loanNotOpen
    }

}
