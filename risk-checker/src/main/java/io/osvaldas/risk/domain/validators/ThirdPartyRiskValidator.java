package io.osvaldas.risk.domain.validators;

import org.springframework.stereotype.Component;

import io.osvaldas.risk.domain.validation.ThirdPartyRiskProviderClient;
import io.osvaldas.risk.domain.validation.ValidationRule;
import io.osvaldas.risk.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ThirdPartyRiskValidator implements ValidationRule {

    private final ThirdPartyRiskProviderClient client;

    @Override
    public void validate(RiskValidationTarget target) {
        client.isApproved(target);
    }

}
