package io.osvaldas.risk.domain.validation;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

import org.springframework.http.HttpStatus;

import io.osvaldas.api.exceptions.ApiRequestException;

public class BackOfficeResponseException extends ApiRequestException {

    public BackOfficeResponseException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_GATEWAY;
    }

}
