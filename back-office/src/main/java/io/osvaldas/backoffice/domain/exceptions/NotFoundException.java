package io.osvaldas.backoffice.domain.exceptions;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ApiRequestException {

    public NotFoundException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
