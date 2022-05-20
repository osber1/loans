package io.osvaldas.backoffice.domain.loans;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import io.osvaldas.backoffice.infra.configuration.feign.CustomFeignClientConfiguration;

@FeignClient(name = "fraud-checker", url = "http://localhost:8081", configuration = CustomFeignClientConfiguration.class)
public interface RiskCheckerClient {

    @PostMapping("api/v1/validation")
    RiskValidationResponse validate(RiskValidationRequest request);

}
