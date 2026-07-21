package io.osvaldas.risk.domain.validation

import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class ThirdPartyRiskProviderClientImplSpec extends AbstractSpec {

    @Subject
    ThirdPartyRiskProviderClient client = new ThirdPartyRiskProviderClientImpl()

    void 'implementation satisfies ThirdPartyRiskProviderClient contract and returns a result for a target'() {
        expect:
            client instanceof ThirdPartyRiskProviderClient
        and:
            client.isApproved(new RiskValidationTarget(loanAmount: 100.00, clientId: clientId)) == true
    }

    void 'always returns a positive result and never throws'() {
        expect:
            client.isApproved(target) == true

        where:
            target << [
                new RiskValidationTarget(),
                new RiskValidationTarget(loanAmount: 0.00, clientId: ''),
                new RiskValidationTarget(loanAmount: 90000000000000.00, clientId: clientId),
                new RiskValidationTarget(loanAmount: 100.00, clientId: clientId),
            ]
    }

}
