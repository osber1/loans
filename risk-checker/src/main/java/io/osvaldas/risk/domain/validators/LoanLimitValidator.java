package io.osvaldas.risk.domain.validators;

import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException;
import io.osvaldas.api.loans.TodayTakenLoansCount;
import io.osvaldas.risk.domain.validation.BackOfficeClient;
import io.osvaldas.risk.domain.validation.ValidationRule;
import io.osvaldas.risk.infra.configuration.PropertiesConfig;
import io.osvaldas.risk.repositories.risk.RiskValidationTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Rejects the loan when the client has already reached the daily loan limit.
 *
 * <p>Back-office reports how many loans the client has taken today <em>other than</em> the loan being
 * evaluated, so the evaluated loan is rejected once that count reaches the limit.
 *
 * <p>Calls back-office, so it runs after the local rules.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(LoanLimitValidator.ORDER)
public class LoanLimitValidator implements ValidationRule {

    static final int ORDER = TimeAndAmountValidator.ORDER + 1;

    private final PropertiesConfig config;

    private final BackOfficeClient client;

    @Override
    public void validate(RiskValidationTarget target) {
        TodayTakenLoansCount count = client.getLoansTakenTodayCount(target.getClientId());
        if (count == null) {
            log.warn("Back-office returned no taken loans count for client {}, skipping loan limit check.", target.getClientId());
            return;
        }
        if (count.takenLoansCount() >= config.getLoanLimitPerDay()) {
            throw new LoanLimitException(LOAN_LIMIT_EXCEEDS);
        }
    }

}
