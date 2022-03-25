package io.osvaldas.loans.domain.exceptions;

import static java.time.ZonedDateTime.now;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class ApiRequestException extends RuntimeException {

    protected ApiRequestException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();

    public ApiExceptionResource toResource() {
        return toResource(this.getMessage());
    }

    public ApiExceptionResource toResource(String message) {
        return new ApiExceptionResource(message, now(), this.getHttpStatus());
    }

    @Getter
    @AllArgsConstructor
    public static class ApiExceptionResource {

        private final String message;

        private final ZonedDateTime timestamp;

        private final HttpStatus status;

    }
}
