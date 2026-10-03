package io.osvaldas.backoffice

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

import spock.lang.Specification

@ActiveProfiles('test')
@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = BackOfficeApplication)
class BackOfficeApplicationSpec extends Specification {

    @Autowired
    ApplicationContext context

    @Value('${spring.jpa.open-in-view}')
    boolean openInView

    void 'should load context'() {
        expect:
            context
    }

    void 'should run tests with open-in-view disabled as in production'() {
        expect:
            !openInView
    }

}
