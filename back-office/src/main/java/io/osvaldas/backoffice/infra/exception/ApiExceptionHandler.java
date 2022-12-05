package io.osvaldas.backoffice.infra.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.osvaldas.api.exceptions.ApiRequestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiRequestException.class)
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(e.toResource(), e.getHttpStatus());
    }
}
