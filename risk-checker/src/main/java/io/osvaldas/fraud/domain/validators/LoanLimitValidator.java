package io.osvaldas.fraud.domain.validators;

import static java.util.Optional.of;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException;
import io.osvaldas.api.loans.TodayTakenLoansCount;
import io.osvaldas.fraud.domain.validation.BackOfficeClient;
import io.osvaldas.fraud.domain.validation.ValidationRule;
import io.osvaldas.fraud.infra.configuration.PropertiesConfig;
import io.osvaldas.fraud.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanLimitValidator implements ValidationRule {

    private final PropertiesConfig config;

    private final BackOfficeClient client;

    @Value("${exceptionMessages.loanLimitExceedsMessage:}")
    private String loanLimitExceedsMessage;

    @Override
    public void validate(RiskValidationTarget target) {
        of(client.getLoansTakenTodayCount(target.getClientId()))
            .map(TodayTakenLoansCount::getTakenLoansCount)
            .filter(count -> count > config.getLoanLimitPerDay())
            .ifPresent(count -> {
                throw new LoanLimitException(loanLimitExceedsMessage);
            });
    }

}
