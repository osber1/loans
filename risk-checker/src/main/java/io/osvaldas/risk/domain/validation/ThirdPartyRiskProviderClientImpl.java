package io.osvaldas.risk.domain.validation;

import org.springframework.stereotype.Component;

import io.osvaldas.risk.repositories.risk.RiskValidationTarget;

@Component
public class ThirdPartyRiskProviderClientImpl implements ThirdPartyRiskProviderClient {

    @Override
    public boolean isApproved(RiskValidationTarget target) {
        return true;
    }

}
