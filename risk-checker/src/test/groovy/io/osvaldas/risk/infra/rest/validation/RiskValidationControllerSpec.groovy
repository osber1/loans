package io.osvaldas.risk.infra.rest.validation

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS
import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS
import static io.osvaldas.risk.domain.validators.LoanLimitValidator.NO_LOANS_COUNT
import static io.osvaldas.risk.infra.exception.ApiExceptionHandler.BACK_OFFICE_ERROR
import static io.osvaldas.risk.infra.exception.ApiExceptionHandler.BACK_OFFICE_UNAVAILABLE
import static java.time.Clock.fixed
import static java.time.Instant.parse
import static java.time.ZoneId.of
import static org.springframework.http.HttpStatus.BAD_GATEWAY
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration

import groovy.json.JsonBuilder
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.risk.infra.rest.AbstractControllerSpec
import spock.lang.Shared

@ContextConfiguration(classes = TestClockConfig)
@SpringBootTest(properties = 'spring.main.allow-bean-definition-overriding=true')
class RiskValidationControllerSpec extends AbstractControllerSpec {

    @Shared
    long validLoanId = 1

    @Shared
    long tooHighAmountLoanId = 2

    @Shared
    long maxAmountLoanId = 3

    @Shared
    long backOfficeErrorLoanId = 4

    @Shared
    long slowLoanId = 5

    @Shared
    String validClientId = 'clientId'

    @Shared
    String tooMuchLoansClientId = 'tooMuchLoansClientId'

    @Shared
    String backOfficeErrorClientId = 'backOfficeErrorClientId'

    @Shared
    String noCountClientId = 'noCountClientId'

    @Shared
    String loansTakenTodayPath = '/api/v1/loans/today'

    void setup() {
        testClockDelegate.changeDelegate(fixed(parse('2022-10-12T10:10:10.00Z'), of('UTC')))
    }

    void 'should return success when amount is not too high'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(validLoanId, validClientId)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(readValidationResponse(response)) {
                success()
            }
    }

    void 'should fail when amount is too high'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(tooHighAmountLoanId, validClientId)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(readValidationResponse(response)) {
                !success()
                message() == AMOUNT_EXCEEDS
            }
    }

    void 'should not call back-office loan limit check when local rule rejects loan'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(tooHighAmountLoanId, tooMuchLoansClientId)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            with(readValidationResponse(response)) {
                message() == AMOUNT_EXCEEDS
            }
        and:
            wireMock.verifyThat(0, getRequestedFor(urlPathEqualTo(loansTakenTodayPath)))
    }

    void 'should fail when loan limit is reached'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(validLoanId, tooMuchLoansClientId)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(readValidationResponse(response)) {
                !success()
                message() == LOAN_LIMIT_EXCEEDS
            }
    }

    void 'should fail when max amount and forbidden time'() {
        given:
            testClockDelegate.changeDelegate(fixed(parse('2022-10-12T04:10:10.00Z'), of('UTC')))
        and:
            RiskValidationRequest request = new RiskValidationRequest(maxAmountLoanId, validClientId)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(readValidationResponse(response)) {
                !success()
                message() == riskTooHigh
            }
    }

    void 'should return #status when back-office fails for loan #loanId and client #clientId'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(loanId, clientId)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == status.value()
            MediaType.parseMediaType(response.contentType).isCompatibleWith(APPLICATION_PROBLEM_JSON)
        and:
            Map problem = readProblem(response)
            problem.status == status.value()
            problem.detail == detail
        where:
            loanId                | clientId                || status              | detail
            backOfficeErrorLoanId | validClientId           || BAD_GATEWAY         | BACK_OFFICE_ERROR
            validLoanId           | backOfficeErrorClientId || SERVICE_UNAVAILABLE | BACK_OFFICE_UNAVAILABLE
            slowLoanId            | validClientId           || SERVICE_UNAVAILABLE | BACK_OFFICE_UNAVAILABLE
            validLoanId           | noCountClientId         || BAD_GATEWAY         | NO_LOANS_COUNT
    }

    void 'should return bad request when request is invalid: #description'() {
        when:
            MockHttpServletResponse response = postValidationRequest(body)
        then:
            response.status == BAD_REQUEST.value()
            MediaType.parseMediaType(response.contentType).isCompatibleWith(APPLICATION_PROBLEM_JSON)
        and:
            readProblem(response).status == BAD_REQUEST.value()
        and:
            wireMock.serveEvents.empty
        where:
            body                                                           | description
            new JsonBuilder([loanId: validLoanId]) as String               | 'missing client id'
            new JsonBuilder([loanId: validLoanId, clientId: '']) as String | 'empty client id'
            new JsonBuilder([clientId: validClientId]) as String           | 'missing loan id'
            '{not json'                                                    | 'malformed body'
    }

    MockHttpServletResponse postValidationRequest(RiskValidationRequest request) {
        postValidationRequest(new JsonBuilder(request) as String)
    }

    MockHttpServletResponse postValidationRequest(String body) {
        mockMvc.perform(post('/api/v1/validation')
            .content(body)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

    RiskValidationResponse readValidationResponse(MockHttpServletResponse response) {
        objectMapper.readValue(response.contentAsString, RiskValidationResponse)
    }

    Map readProblem(MockHttpServletResponse response) {
        objectMapper.readValue(response.contentAsString, Map)
    }

}
