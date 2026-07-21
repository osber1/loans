package io.osvaldas.risk.domain.validators

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment

import io.osvaldas.risk.RiskCheckerApplication
import io.osvaldas.risk.domain.validation.ValidationRule
import spock.lang.Specification

@SpringBootTest(webEnvironment = WebEnvironment.NONE, classes = RiskCheckerApplication)
class RiskValidatorSpec extends Specification {

    @Autowired
    RiskValidator riskValidator

    @Autowired
    List<ValidationRule> rules

    void 'rule chain includes ThirdPartyRiskValidator alongside LoanLimitValidator and TimeAndAmountValidator'() {
        expect:
            riskValidator
        and:
            rules*.class.containsAll([LoanLimitValidator, TimeAndAmountValidator, ThirdPartyRiskValidator])
    }

}
