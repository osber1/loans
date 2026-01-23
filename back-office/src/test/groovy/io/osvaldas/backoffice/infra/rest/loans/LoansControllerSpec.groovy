package io.osvaldas.backoffice.infra.rest.loans

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor
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
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.annotation.Transactional
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock

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

@EnableWireMock([@ConfigureWireMock(port = 8081)])
@ContextConfiguration(classes = TestClockConfig)
@SpringBootTest(properties = 'spring.main.allow-bean-definition-overriding=true')
class LoansControllerSpec extends AbstractControllerSpec {

    @Shared
    LoanRequest loanRequest = buildLoanRequest(100.00)

    void setupSpec() {
        configureFor('localhost', 8081)
    }

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

    void 'should return loans when client exists'() {
        given:
            Client client = buildClient(CLIENT_ID, [buildLoanWithoutId(100.0)] as Set, ACTIVE)
        and:
            Client savedClient = clientRepository.save(client)
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans')
                .param('clientId', savedClient.id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            ([objectMapper.readValue(response.contentAsString, LoanResponse[])] as Set).size() == 1
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

    void 'should take loan when request is correct'() {
        given:
            clientRepository.save(activeClientWithId)
        and:
            RiskValidationResponse validationResponse = new RiskValidationResponse(true, 'Risk validation passed.')
            stubWireMockResponse(validationResponse)
        when:
            MockHttpServletResponse response = postLoanRequest(loanRequest, CLIENT_ID)
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
            RiskValidationResponse validationResponse = new RiskValidationResponse(false, LOAN_LIMIT_EXCEEDS)
            stubWireMockResponse(validationResponse)
        when:
            postLoanRequest(loanRequest, CLIENT_ID)
            MockHttpServletResponse response = postLoanRequest(loanRequest, CLIENT_ID)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(LOAN_LIMIT_EXCEEDS)
        and:
            REJECTED == loanRepository.findAllByClient(activeClientWithId).last().status
    }

    @Transactional
    void 'should fail when amount is too high'() {
        given:
            clientRepository.save(activeClientWithId)
        and:
            RiskValidationResponse validationResponse = new RiskValidationResponse(false, AMOUNT_EXCEEDS)
            stubWireMockResponse(validationResponse)
        when:
            MockHttpServletResponse response = postLoanRequest(buildLoanRequest(999999.0), CLIENT_ID)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(AMOUNT_EXCEEDS)
        and:
            REJECTED == loanRepository.findAllByClient(activeClientWithId).last().status
    }

    @Transactional
    void 'should fail when max amount and forbidden time'() {
        given:
            clientRepository.save(activeClientWithId)
        and:
            RiskValidationResponse validationResponse = new RiskValidationResponse(false, RISK_TOO_HIGH)
            stubWireMockResponse(validationResponse)
        when:
            MockHttpServletResponse response = postLoanRequest(buildLoanRequest(50.0), CLIENT_ID)
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(RISK_TOO_HIGH)
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

    void 'should return loans taken today count'() {
        given:
            clientRepository.save(buildClient(CLIENT_ID, [buildLoanWithoutId(100.0)] as Set, ACTIVE))
        when:
            MockHttpServletResponse response = mockMvc.perform(get('/api/v1/loans/today')
                .param('clientId', CLIENT_ID)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            response.contentAsString.contains('1')
    }

    private StubMapping stubWireMockResponse(RiskValidationResponse response) {
        stubFor(WireMock.post(urlPathEqualTo('/api/v1/validation'))
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
