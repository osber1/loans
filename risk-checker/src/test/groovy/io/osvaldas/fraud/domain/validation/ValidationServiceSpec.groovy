package io.osvaldas.fraud.domain.validation

import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.fraud.repositories.risk.RiskValidationTarget
import spock.lang.Specification
import spock.lang.Subject

class ValidationServiceSpec extends Specification {

    Validator validator = Mock()

    BackOfficeClient backOfficeClient = Stub {
        getLoan(_ as Long) >> new LoanResponse()
    }

    @Subject
    ValidationService validationService = new ValidationService(validator, backOfficeClient)

    void 'should call validator when checking risk'() {
        given:
            RiskValidationRequest request = new RiskValidationRequest()
        when:
            validationService.validate(request)
        then:
            1 * validator.validate(_ as RiskValidationTarget)
    }

}
