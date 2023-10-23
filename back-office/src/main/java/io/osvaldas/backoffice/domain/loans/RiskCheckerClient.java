package io.osvaldas.backoffice.domain.loans;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import jakarta.validation.Valid;

@FeignClient(name = "risk-checker", url = "${risk.checker.url:}")
public interface RiskCheckerClient {

    @PostMapping("api/v1/validation")
    RiskValidationResponse validate(@Valid @RequestBody RiskValidationRequest request);

}
