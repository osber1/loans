package io.osvaldas.loans.domain.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = { BadRequestException.class, NotFoundException.class })
    public ResponseEntity<Object> handleApiRequestException(ApiRequestException e) {
        return new ResponseEntity<>(e.toResource(), e.getHttpStatus());
    }
}
