package io.osvaldas.risk.domain.validators;

import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS;

import java.util.Optional;

import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException;
import io.osvaldas.api.loans.TodayTakenLoansCount;
import io.osvaldas.risk.domain.validation.BackOfficeClient;
import io.osvaldas.risk.domain.validation.ValidationRule;
import io.osvaldas.risk.infra.configuration.PropertiesConfig;
import io.osvaldas.risk.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanLimitValidator implements ValidationRule {

    private final PropertiesConfig config;

    private final BackOfficeClient client;

    @Override
    public void validate(RiskValidationTarget target) {
        Optional.of(client.getLoansTakenTodayCount(target.getClientId()))
            .map(TodayTakenLoansCount::takenLoansCount)
            .filter(count -> count > config.getLoanLimitPerDay())
            .ifPresent(count -> {
                throw new LoanLimitException(LOAN_LIMIT_EXCEEDS);
            });
    }

}
