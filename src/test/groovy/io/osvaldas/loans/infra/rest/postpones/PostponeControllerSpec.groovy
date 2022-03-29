package io.osvaldas.loans.infra.rest.postpones

import io.osvaldas.loans.infra.rest.AbstractControllerSpec

class PostponeControllerSpec extends AbstractControllerSpec {

    //    void 'should postpone loan when it exists'() {
//        given:
//            Client savedClient = clientRepository.save(client)
//        when:
//            MockHttpServletResponse response = mockMvc.perform(post('/api/client/loans/{id}/extensions', savedClient.getLoans()[0].getId())
//                .contentType(MediaType.APPLICATION_JSON))
//                .andReturn().response
//        then:
//            response.status == HttpStatus.OK.value()
//        and:
//            LoanPostpone postpone = objectMapper.readValue(response.contentAsString, LoanPostpone.class)
//            with(postpone) {
//                id == loanPostpone.id
//                newInterestRate == loanPostpone.newInterestRate
//            }
//    }
//
//    void 'should throw an exception when trying to postpone loan'() {
//        when:
//            MockHttpServletResponse response = mockMvc.perform(post('/api/client/loans/{id}/extensions', 5555)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andReturn().response
//        then:
//            response.status == HttpStatus.NOT_FOUND.value()
//        and:
//            response.contentAsString.contains('Loan with id 5555 does not exist.')
//    }
}
