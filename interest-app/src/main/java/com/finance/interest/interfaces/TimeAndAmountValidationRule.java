package com.finance.interest.interfaces;

import java.math.BigDecimal;

public interface TimeAndAmountValidationRule {

    void validate(BigDecimal amount);

    class ValidationRuleException extends RuntimeException {

        public ValidationRuleException(String message) {
            super(message);
        }
    }
}
