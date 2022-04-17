package io.osvaldas.fraud.infra.rest.validation

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration

import groovy.json.JsonBuilder
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.fraud.infra.rest.AbstractControllerSpec

@ContextConfiguration(classes = TestClockConfig)
@SpringBootTest(properties = 'spring.main.allow-bean-definition-overriding=true')
class RiskValidationControllerSpec extends AbstractControllerSpec {

    void 'should fail when amount is too high'() {
        when:
            MockHttpServletResponse response = postValidationRequest(new RiskValidationRequest(1, '111'))
        then:
            response.status == BAD_REQUEST.value()
        and:
            response.contentAsString.contains(amountExceedsMessage)
    }

    void 'should return success when amount is not too high'() {
        when:
            MockHttpServletResponse response = postValidationRequest(new RiskValidationRequest(1, '111'))
        then:
            response.status == OK.value()
        and:
            response.contentAsString.contains('Risk validation passed')
    }

    MockHttpServletResponse postValidationRequest(RiskValidationRequest request) {
        mockMvc.perform(post('/api/v1/validation')
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

}
