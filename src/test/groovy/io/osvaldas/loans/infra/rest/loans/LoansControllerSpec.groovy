package io.osvaldas.loans.infra.rest.loans

import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.mock.web.MockHttpServletResponse

import groovy.json.JsonBuilder
import io.osvaldas.loans.infra.rest.AbstractControllerSpec
import io.osvaldas.loans.infra.rest.loans.dtos.LoanRequest

class LoansControllerSpec extends AbstractControllerSpec {

    //    void 'should fail when ip limit is exceeded taking loans'() {
//        given:
//            LoanRequest loanRequest = new LoanRequest().tap {
//                amount = 50
//                termInMonths = 5
//            }
//        when:
//            postLoanRequest(loanRequest, ID)
//            postLoanRequest(loanRequest, ID)
//        then:
//            IpException exception = thrown()
//            exception.message == 'Too many requests from the same ip per day.'
//    }
//
//    void 'should take loan when request is correct'() {
//        given:
//            clientRepository.save(client)
//            LoanRequest request = new LoanRequest().tap {
//                amount = 50
//                termInMonths = 5
//            }
//        when:
//            MockHttpServletResponse response = postLoanRequest(request, ID)
//        then:
//            response.status == HttpStatus.OK.value()
//        and:
//            LoanResponse loanResponse = objectMapper.readValue(response.contentAsString, LoanResponse)
//            with(loanResponse) {
//                amount == request.amount
//                termInMonths == request.termInMonths
//            }
//    }
//
//and:
//    List<Loan> responseLoanList = new JsonSlurper().parseText(response.contentAsString) as List<Loan>
//    Loan actualLoan = client.loans[0]
//    with(responseLoanList.first()) {
//        amount == actualLoan.amount
//        interestRate == actualLoan.interestRate
//        termInMonths == actualLoan.termInMonths
//        returnDate.toString() == actualLoan.returnDate.toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS).toString()
//    }

    MockHttpServletResponse postLoanRequest(LoanRequest request, String id) {
        mockMvc.perform(post('/api/client/' + id + '/loan')
            .requestAttr('remoteAddr', '0.0.0.0.0.1')
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }
}
