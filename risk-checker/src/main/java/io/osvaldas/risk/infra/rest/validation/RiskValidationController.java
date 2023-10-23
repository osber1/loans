package io.osvaldas.risk.infra.rest.validation;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import io.osvaldas.risk.domain.validation.ValidationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class RiskValidationController {

    private final ValidationService validationService;

    @PostMapping("validation")
    public RiskValidationResponse validateRisk(@RequestBody @Valid RiskValidationRequest request) {
        return validationService.validate(request);
    }

}
