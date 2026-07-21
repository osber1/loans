package io.osvaldas.risk.domain.validation;

import io.osvaldas.risk.repositories.risk.RiskValidationTarget;

public interface ThirdPartyRiskProviderClient {

    boolean isApproved(RiskValidationTarget target);

}
