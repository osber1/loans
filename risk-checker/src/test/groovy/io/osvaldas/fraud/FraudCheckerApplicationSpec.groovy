package io.osvaldas.fraud

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.ApplicationContext

import spock.lang.Specification

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = FraudCheckerApplication)
class FraudCheckerApplicationSpec extends Specification {

    @Autowired
    ApplicationContext context

    void 'should load context'() {
        expect:
            context
    }

}
