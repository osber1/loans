package io.osvaldas.backoffice.domain.exceptions;

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

}
