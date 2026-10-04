package io.osvaldas.risk.domain.validation

import static io.osvaldas.api.risk.validation.RiskRejectionReason.AMOUNT_EXCEEDS
import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS as AMOUNT_EXCEEDS_MESSAGE

import java.time.ZonedDateTime

import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.risk.AbstractSpec
import spock.lang.Shared
import spock.lang.Subject

class ValidationServiceSpec extends AbstractSpec {

    @Shared
    BigDecimal amount = 55.5

    @Shared
    ZonedDateTime requestedAt = ZonedDateTime.parse('2022-10-11T23:30:00Z')

    @Shared
    RiskValidationRequest request = new RiskValidationRequest(1L, clientId, amount, 1L, requestedAt)

    ValidationRule firstRule = Mock()

    ValidationRule secondRule = Mock()

    @Subject
    ValidationService validationService = new ValidationService([firstRule, secondRule])

    void 'should return successful validation when no rule rejects the request'() {
        when:
            RiskValidationResponse response = validationService.validate(request)
        then:
            1 * firstRule.check(request) >> Optional.empty()
        then:
            1 * secondRule.check(request) >> Optional.empty()
        and:
            response.success()
            response.reason() == null
    }

    void 'should return failed validation with reason and skip the remaining rules when a rule rejects the request'() {
        when:
            RiskValidationResponse response = validationService.validate(request)
        then:
            1 * firstRule.check(request) >> Optional.of(new Rejection(AMOUNT_EXCEEDS, AMOUNT_EXCEEDS_MESSAGE))
            0 * secondRule.check(_)
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
            1 * firstRule.check(request) >> { throw failure }
        and:
            IllegalStateException e = thrown()
            e.is(failure)
    }

}
