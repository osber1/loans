package com.finance.loans.domain.exceptions;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiRequestException {

    public BadRequestException() {
        this("Bad request");
    }

    public BadRequestException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
