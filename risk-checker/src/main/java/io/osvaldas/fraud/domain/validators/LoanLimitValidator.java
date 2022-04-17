package io.osvaldas.fraud.domain.validators;

import static java.util.Optional.of;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException;
import io.osvaldas.api.loans.TodayTakenLoansCount;
import io.osvaldas.fraud.domain.rules.LoanLimitValidationRule;
import io.osvaldas.fraud.domain.validation.BackOfficeClient;
import io.osvaldas.fraud.infra.configuration.PropertiesConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoanLimitValidator implements LoanLimitValidationRule {

    private final PropertiesConfig config;

    private final BackOfficeClient client;

    @Value("${exceptionMessages.loanLimitExceedsMessage:}")
    private String loanLimitExceedsMessage;

    @Override
    public void validate(String clientId) {
        of(client.getLoansTakenTodayCount(clientId))
            .map(TodayTakenLoansCount::getTakenLoansCount)
            .filter(count -> count > config.getLoanLimitPerDay())
            .ifPresent(count -> {
                throw new LoanLimitException(loanLimitExceedsMessage);
            });
    }

}
