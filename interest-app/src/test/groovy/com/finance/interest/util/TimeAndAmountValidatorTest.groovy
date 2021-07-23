package com.finance.interest.util

import com.finance.interest.configuration.PropertiesConfig
import com.finance.interest.exception.BadRequestException
import com.finance.interest.interfaces.TimeUtils

import spock.lang.Specification

class TimeAndAmountValidatorTest extends Specification {

    private TimeUtils timeUtils = Mock()

    private PropertiesConfig config = Mock()

    private TimeAndAmountValidator underTest = new TimeAndAmountValidator(config, timeUtils)

    void 'should validate when amount is not to high and correct time'() {
        given:
            timeUtils.getHourOfDay() >> 10
            config.getMaxAmount() >> 100.00
            config.getForbiddenHourFrom() >> 0
            config.getForbiddenHourTo() >> 6
        when:
            underTest.validate(_ as String, 100.00)

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
            underTest.validate(_ as String, 100.00)

        then:
            BadRequestException e = thrown()
            e.message == 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'
    }

    void 'should throw exception when amount exceeds max amount'() {
        given:
            timeUtils.getHourOfDay() >> 10
            config.getMaxAmount() >> 100.00
            config.getForbiddenHourFrom() >> 1
            config.getForbiddenHourTo() >> 6
        when:
            underTest.validate(_ as String, 1000.00)

        then:
            BadRequestException e = thrown()
            e.message == 'The amount you are trying to borrow exceeds the max amount!'
    }
}
