package io.osvaldas.loans.infra.rest.clients

import io.osvaldas.loans.infra.rest.AbstractControllerSpec

class ClientControllerSpec extends AbstractControllerSpec {

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
//    void 'should throw exception when client\'s first name is null'() {
//        given:
//            ClientRequest clientRequest = buildClientRequest(null, SURNAME, PERSONAL_CODE, EMAIL, PHONE_NUMBER)
//        when:
//            MockHttpServletResponse response = postClientRequest(clientRequest)
//        then:
//            response.status == HttpStatus.BAD_REQUEST.value()
//        and:
//            response.contentAsString.contains('First name must be not empty.')
//    }
//
//    void 'should throw exception when client\'s last name is null'() {
//        given:
//            ClientRequest clientRequest = buildClientRequest(NAME, null, PERSONAL_CODE, EMAIL, PHONE_NUMBER)
//        when:
//            MockHttpServletResponse response = postClientRequest(clientRequest)
//        then:
//            response.status == HttpStatus.BAD_REQUEST.value()
//        and:
//            response.contentAsString.contains('Last name must be not empty.')
//    }
//
//    void 'should throw exception when client\'s personal code is null'() {
//        given:
//            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, null, EMAIL, PHONE_NUMBER)
//        when:
//            MockHttpServletResponse response = postClientRequest(clientRequest)
//        then:
//            response.status == HttpStatus.BAD_REQUEST.value()
//        and:
//            response.contentAsString.contains('Personal code must be not empty.')
//    }
//
//    void 'should throw exception when client\'s personal code is too short'() {
//        given:
//            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, '123456789', EMAIL, PHONE_NUMBER)
//        when:
//            MockHttpServletResponse response = postClientRequest(clientRequest)
//        then:
//            response.status == HttpStatus.BAD_REQUEST.value()
//        and:
//            response.contentAsString.contains('Personal code must be 11 digits length.')
//    }
//
//    void 'should throw exception when client\'s personal code contains letters'() {
//        given:
//            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, '123456789aa', EMAIL, PHONE_NUMBER)
//        when:
//            MockHttpServletResponse response = postClientRequest(clientRequest)
//        then:
//            response.status == HttpStatus.BAD_REQUEST.value()
//        and:
//            response.contentAsString.contains('All characters must be digits.')
//    }
//
//    void 'should throw exception when client\'s email is null'() {
//        given:
//            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, PERSONAL_CODE, null, PHONE_NUMBER)
//        when:
//            MockHttpServletResponse response = postClientRequest(clientRequest)
//        then:
//            response.status == HttpStatus.BAD_REQUEST.value()
//        and:
//            response.contentAsString.contains('Email must be not empty.')
//    }
//
//    void 'should throw exception when client\'s email is wrong'() {
//        given:
//            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, PERSONAL_CODE, 'bad email', PHONE_NUMBER)
//        when:
//            MockHttpServletResponse response = postClientRequest(clientRequest)
//        then:
//            response.status == HttpStatus.BAD_REQUEST.value()
//        and:
//            response.contentAsString.contains('must be a well-formed email address')
//    }
//
//    void 'should throw exception when client\'s phone number is null'() {
//        given:
//            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, PERSONAL_CODE, EMAIL, null)
//        when:
//            MockHttpServletResponse response = postClientRequest(clientRequest)
//        then:
//            response.status == HttpStatus.BAD_REQUEST.value()
//        and:
//            response.contentAsString.contains('Phone number must be not empty.')
//    }
//
//    void 'should return client history when it exists'() {
//        given:
//            clientRepository.save(client)
//        when:
//            MockHttpServletResponse response = mockMvc.perform(get('/api/client/{id}/loans', ID)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andReturn().response
//        then:
//            response.status == HttpStatus.OK.value()
//        and:
//            List<Loan> responseLoanList = new JsonSlurper().parseText(response.contentAsString) as List<Loan>
//            Loan actualLoan = client.loans[0]
//            with(responseLoanList.first()) {
//                amount == actualLoan.amount
//                interestRate == actualLoan.interestRate
//                termInMonths == actualLoan.termInMonths
//                returnDate.toString() == actualLoan.returnDate.toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS).toString()
//            }
//    }
//
//    void 'should throw an exception when trying to get client history and it not exist'() {
//        when:
//            MockHttpServletResponse response = mockMvc.perform(get('/api/client/{id}/loans', 5555)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andReturn().response
//        then:
//            response.status == HttpStatus.NOT_FOUND.value()
//        and:
//            response.contentAsString.contains('Client with id 5555 does not exist.')
//    }
//
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