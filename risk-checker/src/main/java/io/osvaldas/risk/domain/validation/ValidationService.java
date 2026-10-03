package io.osvaldas.risk.domain.validation;

import org.springframework.stereotype.Service;

import io.osvaldas.api.exceptions.ValidationRuleException;
import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import io.osvaldas.risk.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs the risk validation rules for a loan.
 *
 * <p>Only business rejections ({@link ValidationRuleException}) are turned into an unsuccessful
 * {@link RiskValidationResponse}. Infrastructure failures (e.g. back-office being unavailable) are
 * propagated, so the caller gets an error status instead of a permanent rejection.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final Validator validator;

    private final BackOfficeClient backOfficeClient;

    public RiskValidationResponse validate(RiskValidationRequest request) {
        String clientId = request.clientId();
        long loanId = request.loanId();
        log.info("Validating client {} loan {}.", clientId, loanId);
        LoanResponse loan = backOfficeClient.getLoan(loanId);
        try {
            validator.validate(new RiskValidationTarget(loan.amount(), clientId));
        } catch (ValidationRuleException e) {
            log.info("Risk validation rejected for client {} with loan {}: {}", clientId, loanId, e.getMessage());
            return new RiskValidationResponse(false, e.getMessage());
        }
        log.info("Client {} request successful.", clientId);
        return new RiskValidationResponse(true, "Risk validation passed.");
    }
}
