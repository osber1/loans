package io.osvaldas.backoffice.infra.rest.loans

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.containing
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.REJECTED
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.transaction.annotation.Transactional

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import groovy.json.JsonBuilder
import io.osvaldas.api.loans.LoanRequest
import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.api.risk.validation.RiskRejectionReason
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared

class LoansControllerSpec extends AbstractControllerSpec {

    @Shared
    LoanRequest loanRequest = buildLoanRequest(100.00)

    void 'should return loan when it exists'() {
        given:
            Loan savedLoan = loanRepository.save(buildLoanWithoutId(100.0))
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
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans/{id}', LOAN_ID)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == NOT_FOUND.value()
        and:
            response.contentAsString.contains(LOAN_NOT_FOUND.formatted(LOAN_ID))
    }

    void 'should throw an exception when client not found'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans')
                .param('clientId', CLIENT_ID)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == NOT_FOUND.value()
        and:
            response.contentAsString.contains(CLIENT_NOT_FOUND.formatted(CLIENT_ID))
    }

    @Transactional
    void 'should fail when loan limit is exceeded'() {
        given:
            clientRepository.save(activeClientWithId)
        and:
            RiskValidationResponse validationResponse =
                RiskValidationResponse.rejected(RiskRejectionReason.LOAN_LIMIT_EXCEEDS, LOAN_LIMIT_EXCEEDS)
            stubWireMockResponse(validationResponse)
        when:
            MockHttpServletResponse response = postLoanRequest(loanRequest, CLIENT_ID)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(LOAN_LIMIT_EXCEEDS)
        and:
            REJECTED == loanRepository.findAllByClient(activeClientWithId).last().status
    }

    void 'should fail when client is not active'() {
        given:
            clientRepository.save(registeredClientWithId)
        when:
            MockHttpServletResponse response = postLoanRequest(buildLoanRequest(100.0), CLIENT_ID)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(CLIENT_NOT_ACTIVE)
    }

    void 'should return loan with postpones when open-in-view is disabled'() {
        given:
            Loan savedLoan = saveClientWithPostponedLoan()
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans/{loanId}', savedLoan.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            with(objectMapper.readValue(response.contentAsString, LoanResponse)) {
                id() == savedLoan.id
                loanPostpones()*.interestRate() == [15.00]
            }
    }

    void 'should return client loans with postpones when open-in-view is disabled'() {
        given:
            Loan savedLoan = saveClientWithPostponedLoan()
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans')
                .param('clientId', CLIENT_ID)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            List<LoanResponse> loans = List.of(objectMapper.readValue(response.contentAsString, LoanResponse[]))
            loans*.id() == [savedLoan.id]
            loans.first().loanPostpones()*.interestRate() == [15.00]
    }

    void 'should open taken loan and keep client untouched'() {
        given:
            long clientVersion = clientRepository.save(activeClientWithId).version
        and:
            stubWireMockResponse(RiskValidationResponse.passed())
        when:
            MockHttpServletResponse response = postLoanRequest(loanRequest, CLIENT_ID)
        then:
            response.status == OK.value()
        and:
            LoanResponse loanResponse = objectMapper.readValue(response.contentAsString, LoanResponse)
            loanResponse.status() == OPEN
            loanResponse.amount() == loanRequest.amount()
            loanResponse.termInMonths() == loanRequest.termInMonths()
            loanRepository.findById(loanResponse.id()).get().status == OPEN
        and:
            clientRepository.findById(CLIENT_ID).get().version == clientVersion
    }

    private Loan saveClientWithPostponedLoan() {
        Loan loan = buildLoanWithoutId(100.0)
        loan.postponeLoan(7, 1.5)
        clientRepository.save(buildClient(CLIENT_ID, [loan] as Set, ACTIVE)).loans.first()
    }

    private StubMapping stubWireMockResponse(RiskValidationResponse response) {
        wireMock.stubFor(WireMock.post(urlPathEqualTo('/api/v1/validation'))
            .withRequestBody(containing(CLIENT_ID))
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
