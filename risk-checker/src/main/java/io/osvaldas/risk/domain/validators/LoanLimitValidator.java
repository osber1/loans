package io.osvaldas.risk.domain.validators;

import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException;
import io.osvaldas.api.loans.TodayTakenLoansCount;
import io.osvaldas.risk.domain.validation.BackOfficeClient;
import io.osvaldas.risk.domain.validation.BackOfficeResponseException;
import io.osvaldas.risk.domain.validation.ValidationRule;
import io.osvaldas.risk.infra.configuration.PropertiesConfig;
import io.osvaldas.risk.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(LoanLimitValidator.ORDER)
public class LoanLimitValidator implements ValidationRule {

    public static final String NO_LOANS_COUNT = "Back-office returned no taken loans count.";

    static final int ORDER = TimeAndAmountValidator.ORDER + 1;

    private final PropertiesConfig config;

    private final BackOfficeClient client;

    @Override
    public void validate(RiskValidationTarget target) {
        TodayTakenLoansCount count = client.getLoansTakenTodayCount(target.getClientId());
        if (count == null) {
            log.error("Back-office returned no taken loans count for client {}.", target.getClientId());
            throw new BackOfficeResponseException(NO_LOANS_COUNT);
        }
        if (count.takenLoansCount() >= config.getLoanLimitPerDay()) {
            throw new LoanLimitException(LOAN_LIMIT_EXCEEDS);
        }
    }

}
