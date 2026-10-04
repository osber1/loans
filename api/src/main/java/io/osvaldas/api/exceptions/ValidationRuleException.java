package io.osvaldas.api.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;

import io.osvaldas.api.risk.validation.RiskRejectionReason;

public class ValidationRuleException extends ApiRequestException {

    private final RiskRejectionReason reason;

    public ValidationRuleException(RiskRejectionReason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public RiskRejectionReason getReason() {
        return reason;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
    }

}
