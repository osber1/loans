package io.osvaldas.loans.domain.extensions

import spock.lang.Specification

class ExtensionServiceSpec extends Specification {

    //    void 'should fail loan postpone when loan not exist'() {
//        given:
//            loanRepository.findById(_ as Integer) >> Optional.empty()
//        when:
//            clientService.postponeLoan(1)
//        then:
//            NotFoundException e = thrown()
//            e.message == 'Loan with id 1 does not exist.'
//    }
//
//    void 'should postpone loan when it is first postpone'() {
//        given:
//            loanRepository.findById(_ as Long) >> Optional.of(successfulLoan)
//            loanRepository.save(_ as Loan) >> loanWithPostpone
//        when:
//            LoanPostpone loanPostponeResponse = clientService.postponeLoan(1)
//        then:
//            firstPostpone == loanPostponeResponse
//    }
//
//    void 'should postpone loan when it is not first postpone'() {
//        given:
//            loanRepository.findById(_ as Long) >> Optional.of(loanWithPostpone)
//            loanRepository.save(_ as Loan) >> buildLoanWithPostpones(buildLoan(100.00), firstPostpone, secondPostpone)
//        when:
//            LoanPostpone loanPostponeResponse = clientService.postponeLoan(1)
//        then:
//            secondPostpone == loanPostponeResponse
//    }
}
