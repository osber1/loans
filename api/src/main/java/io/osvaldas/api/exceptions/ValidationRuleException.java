package io.osvaldas.api.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;

public class ValidationRuleException extends ApiRequestException {

    public ValidationRuleException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
    }

    public static class AmountException extends ValidationRuleException {

        public AmountException(String message) {
            super(message);
        }
    }

    public static class TimeException extends ValidationRuleException {

        public TimeException(String message) {
            super(message);
        }
    }

    public static class LoanLimitException extends ValidationRuleException {

        public LoanLimitException(String message) {
            super(message);
        }
    }

}
