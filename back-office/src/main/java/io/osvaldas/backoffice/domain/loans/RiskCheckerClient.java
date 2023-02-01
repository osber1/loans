package io.osvaldas.backoffice.domain.loans;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;

@FeignClient(name = "risk-checker", url = "${risk.checker.url:}")
public interface RiskCheckerClient {

    @PostMapping("api/v1/validation")
    RiskValidationResponse validate(RiskValidationRequest request);

}
