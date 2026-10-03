package io.osvaldas.risk.domain.validators

import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS
import static io.osvaldas.api.util.ExceptionMessages.RISK_TOO_HIGH

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.exceptions.ValidationRuleException.AmountException
import io.osvaldas.api.exceptions.ValidationRuleException.TimeException
import io.osvaldas.api.util.TimeUtils
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.infra.configuration.PropertiesConfig
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class TimeAndAmountValidatorSpec extends AbstractSpec {

    BigDecimal maxAmount = 100.00

    TimeUtils timeUtils = Stub {
        hourOfDay >> 10
    }

    PropertiesConfig config = new PropertiesConfig(
        maxAmount: maxAmount,
        forbiddenHourFrom: 0,
        forbiddenHourTo: 6,
        loanLimitPerDay: 1
    )

    @Subject
    TimeAndAmountValidator timeAndAmountValidator = new TimeAndAmountValidator(config, timeUtils)

    void 'should validate when amount is not to high and correct time'() {
        when:
            timeAndAmountValidator.validate(new RiskValidationTarget(loanAmount: maxAmount))
        then:
            notThrown(ValidationRuleException)
    }

    void 'should throw exception when max amount and forbidden time'() {
        when:
            timeAndAmountValidator.validate(new RiskValidationTarget(loanAmount: maxAmount))
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
            e.message == AMOUNT_EXCEEDS
    }

    void 'should not throw time exception when amount is below max amount in forbidden time'() {
        when:
            timeAndAmountValidator.validate(new RiskValidationTarget(loanAmount: 99.99))
        then:
            timeUtils.hourOfDay >> 3
        and:
            notThrown(ValidationRuleException)
    }

    void 'should reject max amount at hour #hour when forbidden window is #from-#to'() {
        given:
            config.forbiddenHourFrom = from
            config.forbiddenHourTo = to
        when:
            timeAndAmountValidator.validate(new RiskValidationTarget(loanAmount: maxAmount))
        then:
            timeUtils.hourOfDay >> hour
        and:
            TimeException e = thrown()
            e.message == RISK_TOO_HIGH.formatted(fromText, toText)
        where:
            from | to | hour || fromText | toText
            0    | 6  | 0    || '00:00'  | '06:00'
            0    | 6  | 5    || '00:00'  | '06:00'
            22   | 6  | 22   || '22:00'  | '06:00'
            22   | 6  | 23   || '22:00'  | '06:00'
            22   | 6  | 0    || '22:00'  | '06:00'
            22   | 6  | 5    || '22:00'  | '06:00'
            23   | 0  | 23   || '23:00'  | '00:00'
    }

    void 'should accept max amount at hour #hour when forbidden window is #from-#to'() {
        given:
            config.forbiddenHourFrom = from
            config.forbiddenHourTo = to
        when:
            timeAndAmountValidator.validate(new RiskValidationTarget(loanAmount: maxAmount))
        then:
            timeUtils.hourOfDay >> hour
        and:
            notThrown(ValidationRuleException)
        where:
            from | to | hour
            0    | 6  | 6
            0    | 6  | 23
            22   | 6  | 21
            22   | 6  | 6
            23   | 0  | 0
            6    | 6  | 6
    }

}
