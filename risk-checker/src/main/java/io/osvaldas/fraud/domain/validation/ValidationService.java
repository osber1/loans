package io.osvaldas.fraud.domain.validation;

import org.springframework.stereotype.Service;

import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import io.osvaldas.fraud.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final Validator validator;

    private final BackOfficeClient backOfficeClient;

    public RiskValidationResponse validate(RiskValidationRequest request) {
        String clientId = request.getClientId();
        long loanId = request.getLoanId();
        log.info("Validating client {} request.", clientId);
        try {
            LoanResponse loan = backOfficeClient.getLoan(loanId);
            RiskValidationTarget riskValidationTarget = new RiskValidationTarget(loan.getAmount(), clientId);
            validator.validate(riskValidationTarget);
            return new RiskValidationResponse(true, "Risk validation passed.");
        } catch (Exception e) {
            log.error("Risk validation failed for client {} with loan {}", clientId, loanId, e);
            return new RiskValidationResponse(false, e.getMessage());
        }
    }
}
