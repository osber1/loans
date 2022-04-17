package io.osvaldas.api.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;

public class BadRequestException extends ApiRequestException {

    public BadRequestException(String message) {
        super(message);
    }

    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
    }
}
