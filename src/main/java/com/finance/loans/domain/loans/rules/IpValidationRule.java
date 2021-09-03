package com.finance.loans.domain.loans.rules;

public interface IpValidationRule {

    void validate(String ip);

    class ValidationRuleException extends RuntimeException {

        public ValidationRuleException(String message) {
            super(message);
        }
    }
}
