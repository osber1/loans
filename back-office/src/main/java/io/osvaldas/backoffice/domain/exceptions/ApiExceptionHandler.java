package io.osvaldas.backoffice.domain.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = { BadRequestException.class, NotFoundException.class,
        ClientNotActiveException.class, ValidationRuleException.class })
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(e.toResource(), e.getHttpStatus());
    }
}
