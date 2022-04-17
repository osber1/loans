package io.osvaldas.backoffice.infra.rest.loans

import static java.lang.String.format
import static java.util.List.of
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration

import groovy.json.JsonBuilder
import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException
import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.api.loans.LoanRequest
import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared

@ContextConfiguration(classes = TestClockConfig)
@SpringBootTest(properties = 'spring.main.allow-bean-definition-overriding=true')
class LoansControllerSpec extends AbstractControllerSpec {

    @Shared
    LoanRequest loanRequest = buildLoanRequest(100.00)

    void 'should return loan when it exists'() {
        given:
            Loan savedLoan = loanRepository.save(buildLoan(100.0))
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans/{loanId}', savedLoan.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            with(objectMapper.readValue(response.contentAsString, LoanResponse)) {
                id == savedLoan.id
                amount == savedLoan.amount
                interestRate == savedLoan.interestRate
                termInMonths == savedLoan.termInMonths
                returnDate.toLocalDate() == savedLoan.returnDate.toLocalDate()
            }
    }

    void 'should throw an exception when loan not found'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans/{id}', loanId)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == NOT_FOUND.value()
        and:
            response.contentAsString.contains(format(loanErrorMessage, loanId))
    }

    void 'should return loans when client exists'() {
        given:
            Client savedClient = clientRepository.save(registeredClientWithLoan)
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/client/{clientId}/loans', savedClient.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            of(objectMapper.readValue(response.contentAsString, LoanResponse[])).size() == 1
    }

    void 'should throw an exception when client not found'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/client/{clientId}/loans', clientId)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == NOT_FOUND.value()
        and:
            response.contentAsString.contains(format(clientErrorMessage, clientId))
    }

    void 'should take loan when request is correct'() {
        given:
            clientRepository.save(activeClientWithId)
        when:
            MockHttpServletResponse response = postLoanRequest(loanRequest, clientId)
        then:
            response.status == OK.value()
        and:
            LoanResponse loanResponse = objectMapper.readValue(response.contentAsString, LoanResponse)
            with(loanResponse) {
                amount == loanRequest.amount
                termInMonths == loanRequest.termInMonths
            }
    }

    void 'should fail when loan limit is exceeded'() {
        given:
            clientRepository.save(activeClientWithId)
        when:
            postLoanRequest(loanRequest, clientId)
            postLoanRequest(loanRequest, clientId)
        then:
            LoanLimitException e = thrown()
            e.message == loanLimitExceedsMessage
    }

    void 'should fail when amount is too high'() {
        given:
            clientRepository.save(activeClientWithId)
        when:
            MockHttpServletResponse response = postLoanRequest(buildLoanRequest(999999.0), clientId)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(amountExceedsMessage)
    }

    void 'should fail when client is not active'() {
        given:
            clientRepository.save(registeredClientWithId)
        when:
            MockHttpServletResponse response = postLoanRequest(buildLoanRequest(100.0), clientId)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(clientNotActiveMessage)
    }

    void 'should return loans taken today count'() {
        given:
            clientRepository.save(activeClientWithLoan)
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/client/{clientId}/loans/today', clientId)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            response.contentAsString.contains('1')
    }

    MockHttpServletResponse postLoanRequest(LoanRequest request, String id) {
        mockMvc.perform(post('/api/v1/client/' + id + '/loan')
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

}
