package io.osvaldas.loans.domain.loans.validators

import io.osvaldas.loans.AbstractSpec
import io.osvaldas.loans.domain.loans.rules.TimeAndAmountValidationRule.ValidationRuleException
import io.osvaldas.loans.domain.loans.validators.TimeAndAmountValidator.AmountException
import io.osvaldas.loans.domain.loans.validators.TimeAndAmountValidator.TimeException
import io.osvaldas.loans.domain.util.TimeUtils
import io.osvaldas.loans.infra.configuration.PropertiesConfig
import spock.lang.Subject

class TimeAndAmountValidatorSpec extends AbstractSpec {

    private TimeUtils timeUtils = Stub {
        hourOfDay >> 10
    }

    private PropertiesConfig config = Stub {
        maxAmount >> 100.00
        forbiddenHourFrom >> 0
        forbiddenHourTo >> 6
    }

    @Subject
    private TimeAndAmountValidator timeAndAmountValidator = new TimeAndAmountValidator(riskMessage, amountExceedsMessage, config, timeUtils)

    void 'should validate when amount is not to high and correct time'() {
        when:
            timeAndAmountValidator.validate(100.00)
        then:
            notThrown(ValidationRuleException)
    }

    void 'should throw exception when max amount and forbidden time'() {
        when:
            timeAndAmountValidator.validate(100.00)
        then:
            timeUtils.hourOfDay >> 3
        and:
            TimeException e = thrown()
            e.message == riskMessage
    }

    void 'should throw exception when amount exceeds max amount'() {
        when:
            timeAndAmountValidator.validate(1000.00)
        then:
            AmountException e = thrown()
            e.message == amountExceedsMessage
    }
}
