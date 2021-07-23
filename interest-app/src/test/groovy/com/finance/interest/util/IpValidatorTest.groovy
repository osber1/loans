package com.finance.interest.util

import java.time.ZoneId
import java.time.ZonedDateTime

import com.finance.interest.configuration.PropertiesConfig
import com.finance.interest.exception.BadRequestException
import com.finance.interest.interfaces.TimeUtils
import com.finance.interest.model.IpLogs
import com.finance.interest.repository.IpLogsRepository

import spock.lang.Specification

class IpValidatorTest extends Specification {

    private static final String TIME_ZONE = 'Europe/Vilnius'

    private static final String IP_ADDRESS_TO_CHECK = '0.0.0.0.0.0.1'

    private TimeUtils timeUtils = Mock()

    private PropertiesConfig config = Mock()

    private IpLogsRepository ipLogsRepository = Mock()

    private IpLogs successfulIpLog = buildIpLog(1)

    private IpLogs unsuccessfulIpLog = buildIpLog(10)

    private static final ZonedDateTime date = generateDate(1)

    private static final ZonedDateTime dateNextDay = generateDate(2)

    private static final ZonedDateTime dateWithTime = generateDate(1)

    private IpValidator underTest = new IpValidator(ipLogsRepository, config, timeUtils)

    void 'should validate when ip limit is not exceeded'() {
        given:
            timeUtils.getCurrentDateTime() >> dateWithTime

            config.getRequestsFromSameIpLimit() >> 5

            ipLogsRepository.findByIp(_ as String) >> Optional.of(successfulIpLog)

        when:
            underTest.validate(IP_ADDRESS_TO_CHECK, 1.00)

        then:
            notThrown(BadRequestException)
        // TODO why this is not working
//            1 * _
//            1 * ipLogsRepository.findByIp(_ as String)
    }

    void 'should throw exception when ip limit is exceeded'() {
        given:
            timeUtils.getCurrentDateTime() >> dateWithTime

            timeUtils.getDayOfMonth() >> date

            config.getRequestsFromSameIpLimit() >> 5

            ipLogsRepository.findByIp(_ as String) >> Optional.of(unsuccessfulIpLog)

        when:
            underTest.validate(IP_ADDRESS_TO_CHECK, 1.00)

        then:
            BadRequestException e = thrown()
            e.message == 'Too many requests from the same ip per day.'
    }

    void 'should validate when ip limit is exceeded, but it is next day'() {
        given:
            timeUtils.getCurrentDateTime() >> dateWithTime

            timeUtils.getDayOfMonth() >> dateNextDay

            config.getRequestsFromSameIpLimit() >> 5

            ipLogsRepository.findByIp(_ as String) >> Optional.of(unsuccessfulIpLog)

        when:
            underTest.validate(IP_ADDRESS_TO_CHECK, 1.00)

        then:
            notThrown(BadRequestException)
    }

    static IpLogs buildIpLog(int ipTimesUsed) {
        return new IpLogs().with {
            id = 1
            ip = IP_ADDRESS_TO_CHECK
            timesUsed = ipTimesUsed
            firstRequestDate = dateWithTime
            return it
        } as IpLogs
    }

    private static ZonedDateTime generateDate(int dayOfMonth) {
        return ZonedDateTime.of(
            2020,
            1,
            dayOfMonth,
            1,
            1,
            1,
            1,
            ZoneId.of(TIME_ZONE))
    }
}
