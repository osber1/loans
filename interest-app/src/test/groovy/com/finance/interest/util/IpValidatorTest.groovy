package com.finance.interest.util

import java.time.ZoneId
import java.time.ZonedDateTime

import com.finance.interest.configuration.PropertiesConfig
import com.finance.interest.interfaces.TimeUtils
import com.finance.interest.model.IpLog
import com.finance.interest.repository.IpLogsRepository
import com.finance.interest.util.IpValidator.IpException

import spock.lang.Specification
import spock.lang.Subject

class IpValidatorTest extends Specification {

    private static final String TIME_ZONE = 'Europe/Vilnius'

    private static final String IP_ADDRESS_TO_CHECK = '0.0.0.0.0.0.1'

    private TimeUtils timeUtils = Stub()

    private PropertiesConfig config = Stub()

    private IpLogsRepository ipLogsRepository = Mock()

    private IpLog successfulIpLog = buildIpLog(1)

    private IpLog unsuccessfulIpLog = buildIpLog(10)

    private static final ZonedDateTime date = generateDate(1)

    private static final ZonedDateTime dateNextDay = generateDate(2)

    private static final ZonedDateTime dateWithTime = generateDate(1)

    @Subject
    private IpValidator ipValidator = new IpValidator(ipLogsRepository, config, timeUtils)

    void 'should validate when ip limit is not exceeded and log is new'() {
        given:
            timeUtils.currentDateTime >> dateWithTime
            config.requestsFromSameIpLimit >> 5
        when:
            ipValidator.validate(IP_ADDRESS_TO_CHECK)
        then:
            notThrown(IpException)
        and:
            1 * ipLogsRepository.findByIp(_ as String) >> Optional.empty()
        and:
            2 * ipLogsRepository.save(_ as IpLog) >> buildSmallIpLog()
    }

    void 'should validate when ip limit is not exceeded and log is not new'() {
        given:
            timeUtils.currentDateTime >> dateWithTime
            config.requestsFromSameIpLimit >> 5
        when:
            ipValidator.validate(IP_ADDRESS_TO_CHECK)
        then:
            notThrown(IpException)
        and:
            1 * ipLogsRepository.findByIp(_ as String) >> Optional.of(successfulIpLog)
    }

    void 'should throw exception when ip limit is exceeded'() {
        given:
            timeUtils.currentDateTime >> dateWithTime
            timeUtils.dayOfMonth >> date
            config.requestsFromSameIpLimit >> 5
            ipLogsRepository.findByIp(_ as String) >> Optional.of(unsuccessfulIpLog)
        when:
            ipValidator.validate(IP_ADDRESS_TO_CHECK)
        then:
            IpException e = thrown()
        and:
            e.message == 'Too many requests from the same ip per day.'
    }

    void 'should validate when ip limit is exceeded, but it is next day'() {
        given:
            timeUtils.currentDateTime >> dateWithTime
            timeUtils.dayOfMonth >> dateNextDay
            config.requestsFromSameIpLimit >> 5
            ipLogsRepository.findByIp(_ as String) >> Optional.of(unsuccessfulIpLog)
        when:
            ipValidator.validate(IP_ADDRESS_TO_CHECK)
        then:
            notThrown(IpException)
    }

    private static IpLog buildIpLog(int ipTimesUsed) {
        return new IpLog().with {
            id = 1
            ip = IP_ADDRESS_TO_CHECK
            timesUsed = ipTimesUsed
            firstRequestDate = dateWithTime
            return it
        } as IpLog
    }

    private static IpLog buildSmallIpLog() {
        return new IpLog().with {
            id = 1
            ip = IP_ADDRESS_TO_CHECK
            firstRequestDate = dateWithTime
            return it
        } as IpLog
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
