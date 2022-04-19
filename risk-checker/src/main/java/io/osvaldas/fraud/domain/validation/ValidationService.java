package io.osvaldas.fraud.domain.validation;

import org.springframework.stereotype.Service;

import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import io.osvaldas.fraud.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private final Validator validator;

    private final BackOfficeClient backOfficeClient;

    public RiskValidationResponse validate(RiskValidationRequest request) {
        LoanResponse loan = backOfficeClient.getLoan(request.getLoanId());
        RiskValidationTarget riskValidationTarget = new RiskValidationTarget(loan.getAmount(), request.getClientId());
        validator.validate(riskValidationTarget);
        return new RiskValidationResponse(true, "Risk validation passed.");
    }
}
