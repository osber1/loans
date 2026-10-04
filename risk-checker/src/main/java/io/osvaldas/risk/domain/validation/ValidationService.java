package io.osvaldas.risk.domain.validation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final List<ValidationRule> rules;

    public RiskValidationResponse validate(RiskValidationRequest request) {
        String clientId = request.clientId();
        long loanId = request.loanId();
        log.info("Validating client {} loan {}.", clientId, loanId);
        for (ValidationRule rule : rules) {
            Optional<Rejection> rejection = rule.check(request);
            if (rejection.isPresent()) {
                log.info("Risk validation rejected for client {} with loan {}: {}", clientId, loanId, rejection.orElseThrow().message());
                return RiskValidationResponse.rejected(rejection.orElseThrow().reason(), rejection.orElseThrow().message());
            }
        }
        log.info("Client {} request successful.", clientId);
        return RiskValidationResponse.passed();
    }
}
