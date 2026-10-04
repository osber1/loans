package io.osvaldas.risk.domain.validators;

import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS;

import java.util.Optional;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.osvaldas.api.risk.validation.RiskRejectionReason;
import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.risk.domain.validation.Rejection;
import io.osvaldas.risk.domain.validation.ValidationRule;
import io.osvaldas.risk.infra.configuration.PropertiesConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Order(LoanLimitValidator.ORDER)
public class LoanLimitValidator implements ValidationRule {

    static final int ORDER = TimeAndAmountValidator.ORDER + 1;

    private final PropertiesConfig config;

    @Override
    public Optional<Rejection> check(RiskValidationRequest request) {
        if (request.loansTakenToday() >= config.getLoanLimitPerDay()) {
            return Optional.of(new Rejection(RiskRejectionReason.LOAN_LIMIT_EXCEEDS, LOAN_LIMIT_EXCEEDS));
        }
        return Optional.empty();
    }

}
