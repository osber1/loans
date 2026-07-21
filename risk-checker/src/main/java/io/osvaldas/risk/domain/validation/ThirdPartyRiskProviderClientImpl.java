package io.osvaldas.risk.domain.validation;

import java.util.Random;

import org.springframework.stereotype.Component;

import io.osvaldas.risk.repositories.risk.RiskValidationTarget;

@Component
public class ThirdPartyRiskProviderClientImpl implements ThirdPartyRiskProviderClient {

    private final Random random = new Random();

    @Override
    public boolean isApproved(RiskValidationTarget target) {
        return random.nextBoolean();
    }

}
