package io.osvaldas.backoffice.infra.rest.loans

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.containing
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.REJECTED
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import groovy.json.JsonBuilder
import io.osvaldas.api.loans.LoanRequest
import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared

@AutoConfigureWireMock(port = 8081)
@ContextConfiguration(classes = TestClockConfig)
@SpringBootTest(properties = 'spring.main.allow-bean-definition-overriding=true')
class LoansControllerSpec extends AbstractControllerSpec {

    @Shared
    LoanRequest loanRequest = buildLoanRequest(100.00)

    void 'should return loan when it exists'() {
        given:
            Loan savedLoan = loanRepository.save(loan)
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans/{loanId}', savedLoan.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            with(objectMapper.readValue(response.contentAsString, LoanResponse)) {
                id() == savedLoan.id
                amount() == savedLoan.amount
                interestRate() == savedLoan.interestRate
                termInMonths() == savedLoan.termInMonths
                returnDate().toLocalDate() == savedLoan.returnDate.toLocalDate()
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
            response.contentAsString.contains(loanNotFound.formatted(loanId))
    }

    void 'should return loans when client exists'() {
        given:
            Client savedClient = clientRepository.save(buildClient(clientId, Set.of(loan), ACTIVE))
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans')
                .param('clientId', savedClient.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            Set.of(objectMapper.readValue(response.contentAsString, LoanResponse[])).size() == 1
    }

    void 'should throw an exception when client not found'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans')
                .param('clientId', clientId)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == NOT_FOUND.value()
        and:
            response.contentAsString.contains(clientNotFound.formatted(clientId))
    }

    void 'should take loan when request is correct'() {
        given:
            clientRepository.save(activeClientWithId)
        and:
            RiskValidationResponse validationResponse = new RiskValidationResponse(true, 'Risk validation passed.')
            stubWireMockResponse(validationResponse)
        when:
            MockHttpServletResponse response = postLoanRequest(loanRequest, clientId)
        then:
            response.status == OK.value()
        and:
            LoanResponse loanResponse = objectMapper.readValue(response.contentAsString, LoanResponse)
            with(loanResponse) {
                amount() == loanRequest.amount()
                termInMonths() == loanRequest.termInMonths()
            }
    }

    @Transactional
    void 'should fail when loan limit is exceeded'() {
        given:
            clientRepository.save(activeClientWithId)
        and:
            RiskValidationResponse validationResponse = new RiskValidationResponse(false, loanLimitExceeds)
            stubWireMockResponse(validationResponse)
        when:
            postLoanRequest(loanRequest, clientId)
            MockHttpServletResponse response = postLoanRequest(loanRequest, clientId)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(loanLimitExceeds)
        and:
            REJECTED == loanRepository.findAllByClient(activeClientWithId).last().status
    }

    @Transactional
    void 'should fail when amount is too high'() {
        given:
            clientRepository.save(activeClientWithId)
        and:
            RiskValidationResponse validationResponse = new RiskValidationResponse(false, amountExceeds)
            stubWireMockResponse(validationResponse)
        when:
            MockHttpServletResponse response = postLoanRequest(buildLoanRequest(999999.0), clientId)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(amountExceeds)
        and:
            REJECTED == loanRepository.findAllByClient(activeClientWithId).last().status
    }

    @Transactional
    void 'should fail when max amount and forbidden time'() {
        given:
            clientRepository.save(activeClientWithId)
        and:
            RiskValidationResponse validationResponse = new RiskValidationResponse(false, riskTooHigh)
            stubWireMockResponse(validationResponse)
        when:
            MockHttpServletResponse response = postLoanRequest(buildLoanRequest(50.0), clientId)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(riskTooHigh)
        and:
            REJECTED == loanRepository.findAllByClient(activeClientWithId).last().status
    }

    void 'should fail when client is not active'() {
        given:
            clientRepository.save(registeredClientWithId)
        when:
            MockHttpServletResponse response = postLoanRequest(buildLoanRequest(100.0), clientId)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(clientNotActive)
    }

    void 'should return loans taken today count'() {
        given:
            clientRepository.save(buildClient(clientId, Set.of(loan), ACTIVE))
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans/today')
                .param('clientId', clientId)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            response.contentAsString.contains('1')
    }

    private StubMapping stubWireMockResponse(RiskValidationResponse response) {
        stubFor(WireMock.post(urlPathEqualTo('/api/v1/validation'))
            .withRequestBody(containing(clientId))
            .willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON.toString())
                .withStatus(200)
                .withBody(toJson(response))))
    }

    private MockHttpServletResponse postLoanRequest(LoanRequest request, String id) {
        mockMvc.perform(post('/api/v1/loans')
            .param('clientId', id)
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object)
        } catch (IOException e) {
            throw new UncheckedIOException(e)
        }
    }

}
