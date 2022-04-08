package io.osvaldas.backoffice.infra.filter

import javax.servlet.FilterChain
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.loans.rules.IpValidationRule
import io.osvaldas.backoffice.domain.loans.validators.IpValidator.IpException
import spock.lang.Shared
import spock.lang.Subject

class IpValidationFilterSpec extends AbstractSpec {

    @Shared
    String ip = '0.1.1.1.1.0.1'

    HttpServletRequest servletRequest = Stub {
        requestURI >> '/api/v1/client/id/loan'
        method >> 'POST'
        remoteAddr >> ip
    }

    IpValidationRule ipValidationRule = Mock()

    @Subject
    IpValidationFilter filter = new IpValidationFilter(ipValidationRule)

    void 'should fail when ip limit is exceeded taking loans'() {
        when:
            filter.doFilter(servletRequest, Stub(ServletResponse), Stub(FilterChain))
        then:
            notThrown(IpException)
        and:
            1 * ipValidationRule.validate(ip)
    }

    void 'should pass when ip limit not is exceeded taking loans'() {
        when:
            filter.doFilter(servletRequest, Stub(ServletResponse), Stub(FilterChain))
        then:
            IpException e = thrown()
            e.message == ipExceedsMessage
        and:
            1 * ipValidationRule.validate(ip) >> {
                throw new IpException(ipExceedsMessage)
            }
    }
}
