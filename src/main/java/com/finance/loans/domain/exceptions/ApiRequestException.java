package com.finance.loans.domain.exceptions;

import org.springframework.http.HttpStatus;

public abstract class ApiRequestException extends RuntimeException {

    protected ApiRequestException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();
}
