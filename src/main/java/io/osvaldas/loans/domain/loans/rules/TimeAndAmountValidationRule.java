package io.osvaldas.loans.domain.loans.rules;

import java.math.BigDecimal;

public interface TimeAndAmountValidationRule {

    void validate(BigDecimal amount);

    class ValidationRuleException extends RuntimeException {

        public ValidationRuleException(String message) {
            super(message);
        }
    }
}
