package io.osvaldas.risk

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.context.ApplicationContext

import io.osvaldas.risk.domain.validation.ValidationRule
import io.osvaldas.risk.domain.validators.LoanLimitValidator
import io.osvaldas.risk.domain.validators.TimeAndAmountValidator
import spock.lang.Specification

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = RiskCheckerApplication)
class RiskCheckerApplicationSpec extends Specification {

    @Autowired
    ApplicationContext context

    @Autowired
    List<ValidationRule> rules

    void 'should load context'() {
        expect:
            context
    }

    void 'should inject local validation rules before remote ones'() {
        expect:
            rules*.class == [TimeAndAmountValidator, LoanLimitValidator]
    }

}
