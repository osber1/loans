import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.ApplicationContext

import com.finance.interest.InterestApplication

import spock.lang.Specification

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = InterestApplication)
class InterestApplicationTest extends Specification {

    @Autowired
    ApplicationContext context

    def "test context loads"() {
        expect:
            context != null
        and:
            context.containsBean("clientService")
        and:
            context.containsBean("clientRepository")
        and:
            context.containsBean("loanRepository")
        and:
            context.containsBean("validationUtils")
        and:
            context.containsBean("ipLogsRepository")
        and :
            context.containsBean("swaggerConfig")
        and :
            context.containsBean("propertiesConfig")

    }
}