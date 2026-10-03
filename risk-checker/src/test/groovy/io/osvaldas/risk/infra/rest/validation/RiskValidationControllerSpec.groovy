package io.osvaldas.risk.infra.rest.validation

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS
import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS
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

    static final long BELOW_LIMIT = 1

    static final long AT_LIMIT = 2

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
    long unavailableLoanId = 6

    @Shared
    String validClientId = 'clientId'

    void setup() {
        testClockDelegate.changeDelegate(fixed(parse('2022-10-12T10:10:10.00Z'), of('UTC')))
    }

    void 'should return success when amount is not too high'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(validLoanId, validClientId, BELOW_LIMIT)
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
            RiskValidationRequest request = new RiskValidationRequest(tooHighAmountLoanId, validClientId, BELOW_LIMIT)
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

    void 'should reject too high amount before checking loan limit'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(tooHighAmountLoanId, validClientId, AT_LIMIT)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            with(readValidationResponse(response)) {
                message() == AMOUNT_EXCEEDS
            }
        and:
            wireMock.verifyThat(1, getRequestedFor(urlPathEqualTo("/api/v1/loans/${tooHighAmountLoanId}")))
            wireMock.serveEvents.size() == 1
    }

    void 'should fail when loan limit is reached'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(validLoanId, validClientId, AT_LIMIT)
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
            RiskValidationRequest request = new RiskValidationRequest(maxAmountLoanId, validClientId, BELOW_LIMIT)
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

    void 'should return #status when back-office fails for loan #loanId'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(loanId, validClientId, BELOW_LIMIT)
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
            loanId                || status              | detail
            backOfficeErrorLoanId || BAD_GATEWAY         | BACK_OFFICE_ERROR
            unavailableLoanId     || SERVICE_UNAVAILABLE | BACK_OFFICE_UNAVAILABLE
            slowLoanId            || SERVICE_UNAVAILABLE | BACK_OFFICE_UNAVAILABLE
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
            body                                                                | description
            [loanId: validLoanId, loansTakenToday: 0]                           | 'missing client id'
            [loanId: validLoanId, clientId: '', loansTakenToday: 0]             | 'empty client id'
            [clientId: validClientId, loansTakenToday: 0]                       | 'missing loan id'
            [loanId: validLoanId, clientId: validClientId]                      | 'missing loans taken today'
            [loanId: validLoanId, clientId: validClientId, loansTakenToday: -1] | 'negative loans taken today'
            '{not json'                                                         | 'malformed body'
    }

    MockHttpServletResponse postValidationRequest(RiskValidationRequest request) {
        postValidationRequest(new JsonBuilder(request) as String)
    }

    MockHttpServletResponse postValidationRequest(Map body) {
        postValidationRequest(new JsonBuilder(body) as String)
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
