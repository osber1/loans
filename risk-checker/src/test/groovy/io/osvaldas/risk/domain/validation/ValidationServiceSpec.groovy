package io.osvaldas.risk.domain.validation

import io.osvaldas.api.exceptions.BadRequestException
import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Specification
import spock.lang.Subject

class ValidationServiceSpec extends Specification {

    Validator validator = Mock()

    BackOfficeClient backOfficeClient = Stub {
        getLoan(_ as Long) >> new LoanResponse()
    }

    @Subject
    ValidationService validationService = new ValidationService(validator, backOfficeClient)

    void 'should return successful validation when checking risk'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest()
        when:
            RiskValidationResponse response = validationService.validate(request)
        then:
            response.success
        and:
            1 * validator.validate(_ as RiskValidationTarget)
    }

    void 'should return failed validation when checking risk'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest()
        when:
            RiskValidationResponse response = validationService.validate(request)
        then:
            !response.success
        and:
            1 * validator.validate(_ as RiskValidationTarget) >> { throw new BadRequestException('') }
    }

}
