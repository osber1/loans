package com.finance.interest.util

import com.finance.interest.configuration.PropertiesConfig
import com.finance.interest.exception.BadRequestException
import com.finance.interest.interfaces.TimeUtils
import com.finance.interest.util.TimeAndAmountValidator.AmountException
import com.finance.interest.util.TimeAndAmountValidator.TimeException

import spock.lang.Specification
import spock.lang.Subject

class TimeAndAmountValidatorTest extends Specification {

    private TimeUtils timeUtils = Stub()

    private PropertiesConfig config = Stub()

    @Subject
    private TimeAndAmountValidator timeAndAmountValidator = new TimeAndAmountValidator(config, timeUtils)

    void 'should validate when amount is not to high and correct time'() {
        given:
            timeUtils.getHourOfDay() >> 10
            config.getMaxAmount() >> 100.00
            config.getForbiddenHourFrom() >> 0
            config.getForbiddenHourTo() >> 6
        when:
            timeAndAmountValidator.validate(100.00)

        then:
            notThrown(BadRequestException)
    }

    void 'should throw exception when max amount and forbidden time'() {
        given:
            timeUtils.getHourOfDay() >> 3
            config.getMaxAmount() >> 100.00
            config.getForbiddenHourFrom() >> 1
            config.getForbiddenHourTo() >> 6
        when:
            timeAndAmountValidator.validate(100.00)

        then:
            TimeException e = thrown()
            e.message == 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'
    }

    void 'should throw exception when amount exceeds max amount'() {
        given:
            timeUtils.getHourOfDay() >> 10
            config.getMaxAmount() >> 100.00
            config.getForbiddenHourFrom() >> 1
            config.getForbiddenHourTo() >> 6
        when:
            timeAndAmountValidator.validate(1000.00)

        then:
            AmountException e = thrown()
            e.message == 'The amount you are trying to borrow exceeds the max amount!'
    }
}
