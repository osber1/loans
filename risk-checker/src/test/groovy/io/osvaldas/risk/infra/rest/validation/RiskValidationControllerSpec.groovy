package io.osvaldas.risk.infra.rest.validation

import static io.osvaldas.api.risk.validation.RiskRejectionReason.AMOUNT_EXCEEDS as AMOUNT_EXCEEDS_REASON
import static io.osvaldas.api.risk.validation.RiskRejectionReason.FORBIDDEN_TIME
import static io.osvaldas.api.risk.validation.RiskRejectionReason.LOAN_LIMIT_EXCEEDS as LOAN_LIMIT_EXCEEDS_REASON
import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS
import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS
import static java.time.Clock.fixed
import static java.time.Instant.parse
import static java.time.ZoneId.of
import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
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

    static final BigDecimal VALID_AMOUNT = 50.0

    static final BigDecimal MAX_AMOUNT = 100.0

    static final BigDecimal TOO_HIGH_AMOUNT = 100.01

    @Shared
    long validLoanId = 1

    @Shared
    String validClientId = 'clientId'

    void setup() {
        testClockDelegate.changeDelegate(fixed(parse('2022-10-12T10:10:10.00Z'), of('UTC')))
    }

    void 'should return success when amount is not too high'() {
        given:
            RiskValidationRequest request = buildRequest(VALID_AMOUNT, BELOW_LIMIT)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(readValidationResponse(response)) {
                success()
                reason() == null
            }
    }

    void 'should fail when amount is too high'() {
        given:
            RiskValidationRequest request = buildRequest(TOO_HIGH_AMOUNT, BELOW_LIMIT)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(readValidationResponse(response)) {
                !success()
                reason() == AMOUNT_EXCEEDS_REASON
                message() == AMOUNT_EXCEEDS
            }
    }

    void 'should reject too high amount before checking loan limit'() {
        given:
            RiskValidationRequest request = buildRequest(TOO_HIGH_AMOUNT, AT_LIMIT)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            with(readValidationResponse(response)) {
                reason() == AMOUNT_EXCEEDS_REASON
            }
    }

    void 'should fail when loan limit is reached'() {
        given:
            RiskValidationRequest request = buildRequest(VALID_AMOUNT, AT_LIMIT)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(readValidationResponse(response)) {
                !success()
                reason() == LOAN_LIMIT_EXCEEDS_REASON
                message() == LOAN_LIMIT_EXCEEDS
            }
    }

    void 'should fail when max amount and forbidden time'() {
        given:
            testClockDelegate.changeDelegate(fixed(parse('2022-10-12T04:10:10.00Z'), of('UTC')))
        and:
            RiskValidationRequest request = buildRequest(MAX_AMOUNT, BELOW_LIMIT)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(readValidationResponse(response)) {
                !success()
                reason() == FORBIDDEN_TIME
                message() == riskTooHigh
            }
    }

    void 'should return bad request when request is invalid: #description'() {
        when:
            MockHttpServletResponse response = postValidationRequest(body)
        then:
            response.status == BAD_REQUEST.value()
            MediaType.parseMediaType(response.contentType).isCompatibleWith(APPLICATION_PROBLEM_JSON)
        and:
            readProblem(response).status == BAD_REQUEST.value()
        where:
            body                                               | description
            [loanId: 1, amount: 50, loansTakenToday: 0]        | 'missing client id'
            [loanId: 1, clientId: '', amount: 50, loansTakenToday: 0] | 'empty client id'
            [clientId: 'c', amount: 50, loansTakenToday: 0]    | 'missing loan id'
            [loanId: 1, clientId: 'c', loansTakenToday: 0]     | 'missing amount'
            [loanId: 1, clientId: 'c', amount: 0, loansTakenToday: 0] | 'zero amount'
            [loanId: 1, clientId: 'c', amount: 50]             | 'missing loans taken today'
            [loanId: 1, clientId: 'c', amount: 50, loansTakenToday: -1] | 'negative loans taken today'
            '{not json'                                        | 'malformed body'
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

    private RiskValidationRequest buildRequest(BigDecimal amount, long loansTakenToday) {
        new RiskValidationRequest(validLoanId, validClientId, amount, loansTakenToday)
    }

}
