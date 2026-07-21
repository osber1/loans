package io.osvaldas.risk.domain.validators

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.domain.validation.ThirdPartyRiskProviderClient
import io.osvaldas.risk.domain.validation.ValidationRule
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class ThirdPartyRiskValidatorSpec extends AbstractSpec {

    ThirdPartyRiskProviderClient client = Stub {
        isApproved(_ as RiskValidationTarget) >> true
    }

    @Subject
    ThirdPartyRiskValidator thirdPartyRiskValidator = new ThirdPartyRiskValidator(client)

    void 'validate does not throw and does not reject the target for any RiskValidationTarget'() {
        when:
            thirdPartyRiskValidator.validate(new RiskValidationTarget(loanAmount: 100.00, clientId: clientId))
        then:
            notThrown(ValidationRuleException)
    }

    void 'is constructed via constructor injection with ThirdPartyRiskProviderClient as a Stub dependency'() {
        expect:
            thirdPartyRiskValidator instanceof ValidationRule
        and:
            new ThirdPartyRiskValidator(client) != null
    }

}
