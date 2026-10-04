package io.osvaldas.risk.domain.validation;

import org.springframework.stereotype.Service;

import io.osvaldas.api.exceptions.ValidationRuleException;
import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import io.osvaldas.risk.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final Validator validator;

    public RiskValidationResponse validate(RiskValidationRequest request) {
        String clientId = request.clientId();
        long loanId = request.loanId();
        log.info("Validating client {} loan {}.", clientId, loanId);
        try {
            validator.validate(new RiskValidationTarget(request.amount(), clientId, request.loansTakenToday(), request.requestedAt()));
        } catch (ValidationRuleException e) {
            log.info("Risk validation rejected for client {} with loan {}: {}", clientId, loanId, e.getMessage());
            return RiskValidationResponse.rejected(e.getReason(), e.getMessage());
        }
        log.info("Client {} request successful.", clientId);
        return RiskValidationResponse.passed();
    }
}
