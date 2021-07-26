package com.finance.interest.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

import com.finance.interest.model.IpLog

import spock.lang.Specification

@DataJpaTest
class IpLogRepositoryTest extends Specification {

    @Autowired
    private IpLogsRepository underTest

    private IpLog log = createIpLog()

    void 'should return ip log when ip is correct'() {
        given:
            IpLog databaseResponse = underTest.save(log)
        when:
            Optional<IpLog> expected = underTest.findByIp(log.ip)
        then:
            expected.filter(databaseResponse::equals)
    }

    void 'should be empty when ip is incorrect'() {
        when:
            Optional<IpLog> expected = underTest.findByIp(log.ip)
        then:
            expected.isEmpty()
    }

    static IpLog createIpLog() {
        return new IpLog().with {
            id = 1
            ip = '0.0.0.0.1'
            timesUsed = 1
            return it
        } as IpLog
    }
}