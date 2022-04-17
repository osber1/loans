package io.osvaldas.fraud.domain.validators

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.exceptions.ValidationRuleException.AmountException
import io.osvaldas.api.exceptions.ValidationRuleException.TimeException
import io.osvaldas.api.util.TimeUtils
import io.osvaldas.fraud.AbstractSpec
import io.osvaldas.fraud.infra.configuration.PropertiesConfig
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
        timeAndAmountValidator.riskMessage = riskMessage
        timeAndAmountValidator.amountExceedsMessage = amountExceedsMessage
    }

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
