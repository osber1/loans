package io.osvaldas.risk.domain.validators

import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS

import io.osvaldas.api.exceptions.ValidationRuleException.AmountException
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.domain.validation.ValidationRule
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class RiskValidatorSpec extends AbstractSpec {

    ValidationRule firstRule = Mock()

    ValidationRule secondRule = Mock()

    RiskValidationTarget target = new RiskValidationTarget(clientId: clientId)

    @Subject
    RiskValidator riskValidator = new RiskValidator([firstRule, secondRule])

    void 'should run rules in order'() {
        when:
            riskValidator.validate(target)
        then:
            1 * firstRule.validate(target)
        then:
            1 * secondRule.validate(target)
    }

    void 'should not run next rules when rule rejects'() {
        when:
            riskValidator.validate(target)
        then:
            1 * firstRule.validate(target) >> { throw new AmountException(AMOUNT_EXCEEDS) }
            0 * secondRule.validate(_)
        and:
            thrown(AmountException)
    }

}
