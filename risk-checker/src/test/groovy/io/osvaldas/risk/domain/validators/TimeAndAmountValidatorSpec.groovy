package io.osvaldas.risk.domain.validators

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.exceptions.ValidationRuleException.AmountException
import io.osvaldas.api.exceptions.ValidationRuleException.TimeException
import io.osvaldas.api.util.TimeUtils
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.infra.configuration.PropertiesConfig
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class TimeAndAmountValidatorSpec extends AbstractSpec {

    TimeUtils timeUtils = Stub {
        hourOfDay >> 10
    }

    PropertiesConfig config = Stub {
        maxAmount >> 100.00
        forbiddenHourFrom >> 0
        forbiddenHourTo >> 6
    }

    @Subject
    TimeAndAmountValidator timeAndAmountValidator = new TimeAndAmountValidator(config, timeUtils)

    void setup() {
        timeAndAmountValidator.riskTooHigh = riskTooHigh
        timeAndAmountValidator.amountExceeds = amountExceeds
    }

    void 'should validate when amount is not to high and correct time'() {
        when:
            timeAndAmountValidator.validate(new RiskValidationTarget(loanAmount: 100.00))
        then:
            notThrown(ValidationRuleException)
    }

    void 'should throw exception when max amount and forbidden time'() {
        when:
            timeAndAmountValidator.validate(new RiskValidationTarget(loanAmount: 100.00))
        then:
            timeUtils.hourOfDay >> 3
        and:
            TimeException e = thrown()
            e.message == riskTooHigh
    }

    void 'should throw exception when amount exceeds max amount'() {
        when:
            timeAndAmountValidator.validate(new RiskValidationTarget(loanAmount: 90000000000000.00))
        then:
            AmountException e = thrown()
            e.message == amountExceeds
    }

}
