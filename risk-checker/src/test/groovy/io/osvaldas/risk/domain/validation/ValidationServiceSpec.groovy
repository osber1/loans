package io.osvaldas.risk.domain.validation

import static io.osvaldas.api.risk.validation.RiskRejectionReason.AMOUNT_EXCEEDS
import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS as AMOUNT_EXCEEDS_MESSAGE

import io.osvaldas.api.exceptions.ValidationRuleException.AmountException
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Shared
import spock.lang.Subject

class ValidationServiceSpec extends AbstractSpec {

    @Shared
    BigDecimal amount = 55.5

    @Shared
    RiskValidationRequest request = new RiskValidationRequest(1L, clientId, amount, 1L)

    Validator validator = Mock()

    @Subject
    ValidationService validationService = new ValidationService(validator)

    void 'should return successful validation when checking risk'() {
        when:
            RiskValidationResponse response = validationService.validate(request)
        then:
            response.success()
            response.reason() == null
        and:
            1 * validator.validate(new RiskValidationTarget(amount, clientId, 1))
    }

    void 'should return failed validation with reason when validation rule rejects loan'() {
        when:
            RiskValidationResponse response = validationService.validate(request)
        then:
            1 * validator.validate(_ as RiskValidationTarget) >> { throw new AmountException(AMOUNT_EXCEEDS_MESSAGE) }
        and:
            !response.success()
            response.reason() == AMOUNT_EXCEEDS
            response.message() == AMOUNT_EXCEEDS_MESSAGE
    }

    void 'should propagate unexpected failures instead of rejecting loan'() {
        given:
            IllegalStateException failure = new IllegalStateException('boom')
        when:
            validationService.validate(request)
        then:
            1 * validator.validate(_ as RiskValidationTarget) >> { throw failure }
        and:
            IllegalStateException e = thrown()
            e.is(failure)
    }

}
