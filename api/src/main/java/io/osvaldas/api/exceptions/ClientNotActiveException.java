package io.osvaldas.api.exceptions;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.http.HttpStatus;

public class ClientNotActiveException extends ApiRequestException {

    public ClientNotActiveException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
    }
}
