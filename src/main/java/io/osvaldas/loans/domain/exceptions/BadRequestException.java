package io.osvaldas.loans.domain.exceptions;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiRequestException {

    public BadRequestException() {
        this("Bad request");
    }

    public BadRequestException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
    }
}
