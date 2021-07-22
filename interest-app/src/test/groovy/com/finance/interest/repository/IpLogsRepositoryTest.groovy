package com.finance.interest.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

import com.finance.interest.model.IpLogs

import spock.lang.Specification

@DataJpaTest
class IpLogsRepositoryTest extends Specification {

    @Autowired
    private IpLogsRepository underTest

    private IpLogs log = createIpLog()

    void 'should return ip log when ip is correct'() {
        given:
            IpLogs databaseResponse = underTest.save(log)
        when:
            Optional<IpLogs> expected = underTest.findByIp(log.ip)
        then:
            expected.filter(databaseResponse::equals)
    }

    void 'should be empty when ip is incorrect'() {
        when:
            Optional<IpLogs> expected = underTest.findByIp(log.ip)
        then:
            expected.isEmpty()
    }

    static IpLogs createIpLog() {
        return new IpLogs().with {
            id = 1
            ip = '0.0.0.0.1'
            timesUsed = 1
            return it
        } as IpLogs
    }
}