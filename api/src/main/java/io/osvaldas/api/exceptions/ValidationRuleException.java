package io.osvaldas.api.exceptions;

import static io.osvaldas.api.risk.validation.RiskRejectionReason.AMOUNT_EXCEEDS;
import static io.osvaldas.api.risk.validation.RiskRejectionReason.FORBIDDEN_TIME;
import static io.osvaldas.api.risk.validation.RiskRejectionReason.LOAN_LIMIT_EXCEEDS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;

import io.osvaldas.api.risk.validation.RiskRejectionReason;

public class ValidationRuleException extends ApiRequestException {

    private final RiskRejectionReason reason;

    protected ValidationRuleException(RiskRejectionReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public static ValidationRuleException of(RiskRejectionReason reason, String message) {
        if (reason == null) {
            return new ValidationRuleException(null, message);
        }
        return switch (reason) {
            case AMOUNT_EXCEEDS -> new AmountException(message);
            case FORBIDDEN_TIME -> new TimeException(message);
            case LOAN_LIMIT_EXCEEDS -> new LoanLimitException(message);
        };
    }

    public RiskRejectionReason getReason() {
        return reason;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
    }

    public static class AmountException extends ValidationRuleException {

        public AmountException(String message) {
            super(AMOUNT_EXCEEDS, message);
        }
    }

    public static class TimeException extends ValidationRuleException {

        public TimeException(String message) {
            super(FORBIDDEN_TIME, message);
        }
    }

    public static class LoanLimitException extends ValidationRuleException {

        public LoanLimitException(String message) {
            super(LOAN_LIMIT_EXCEEDS, message);
        }
    }

}
