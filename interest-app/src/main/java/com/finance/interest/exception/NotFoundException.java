package com.finance.interest.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiRequestException {

    public NotFoundException() {
        super("Not Found.");
    }

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
