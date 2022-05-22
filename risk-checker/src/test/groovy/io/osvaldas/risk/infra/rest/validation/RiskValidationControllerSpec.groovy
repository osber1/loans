package io.osvaldas.risk.infra.rest.validation

import static java.time.Clock.fixed
import static java.time.Instant.parse
import static java.time.ZoneId.of
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.boot.test.context.SpringBootTest
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
    String validClientId = 'clientId'

    @Shared
    String tooMuchLoansClientId = 'tooMuchLoansClientId'

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
            response.contentAsString.contains('Risk validation passed')
    }

    void 'should fail when amount is too high'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(tooHighAmountLoanId, validClientId)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(objectMapper.readValue(response.contentAsString, RiskValidationResponse)) {
                message == amountExceeds
            }
    }

    void 'should fail when too much loans are taken'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest(validLoanId, tooMuchLoansClientId)
        when:
            MockHttpServletResponse response = postValidationRequest(request)
        then:
            response.status == OK.value()
        and:
            with(objectMapper.readValue(response.contentAsString, RiskValidationResponse)) {
                message == loanLimitExceeds
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
            with(objectMapper.readValue(response.contentAsString, RiskValidationResponse)) {
                message == riskTooHigh
            }
    }

    MockHttpServletResponse postValidationRequest(RiskValidationRequest request) {
        mockMvc.perform(post('/api/v1/validation')
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

}
