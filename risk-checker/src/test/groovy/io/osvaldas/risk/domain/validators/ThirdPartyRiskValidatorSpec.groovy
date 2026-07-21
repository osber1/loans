package io.osvaldas.risk.domain.validators

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.domain.validation.ThirdPartyRiskProviderClient
import io.osvaldas.risk.domain.validation.ValidationRule
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class ThirdPartyRiskValidatorSpec extends AbstractSpec {

    ThirdPartyRiskProviderClient client = Mock()

    @Subject
    ThirdPartyRiskValidator thirdPartyRiskValidator = new ThirdPartyRiskValidator(client)

    void 'validate does not throw and does not reject the target for any RiskValidationTarget'() {
        given:
            RiskValidationTarget target = new RiskValidationTarget(loanAmount: 100.00, clientId: clientId)
        when:
            thirdPartyRiskValidator.validate(target)
        then:
            notThrown(ValidationRuleException)
        and:
            1 * client.isApproved(target) >> true
    }

    void 'is constructed via constructor injection with ThirdPartyRiskProviderClient as a dependency'() {
        expect:
            thirdPartyRiskValidator instanceof ValidationRule
        and:
            new ThirdPartyRiskValidator(client) != null
    }

}
