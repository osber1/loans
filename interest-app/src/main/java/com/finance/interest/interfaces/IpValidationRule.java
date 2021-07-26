package com.finance.interest.interfaces;

public interface IpValidationRule {

    void validate(String ip);

    class ValidationRuleException extends RuntimeException {

        public ValidationRuleException(String message) {
            super(message);
        }
    }
}
