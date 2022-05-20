package io.osvaldas.api.exceptions;

import static java.time.ZonedDateTime.now;

import java.time.ZonedDateTime;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public abstract class ApiRequestException extends RuntimeException {

    protected ApiRequestException(String message) {
        super(message);
    }

    public abstract HttpStatus getHttpStatus();

    public ApiExceptionResource toResource() {
        return toResource(getMessage());
    }

    public ApiExceptionResource toResource(String message) {
        return new ApiExceptionResource(message, now(), getHttpStatus());
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiExceptionResource {

        private String message;

        private ZonedDateTime timestamp;

        private HttpStatus status;

    }
}
