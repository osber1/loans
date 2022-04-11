package io.osvaldas.backoffice.domain.loans.rules;

import java.math.BigDecimal;

public interface TimeAndAmountValidationRule {

    void validate(BigDecimal amount);
}
