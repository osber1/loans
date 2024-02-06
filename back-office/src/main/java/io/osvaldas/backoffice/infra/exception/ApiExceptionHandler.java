package io.osvaldas.backoffice.infra.exception;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.osvaldas.api.exceptions.ApiRequestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiRequestException.class)
    public ProblemDetail handleApiRequestException(ApiRequestException e) {
        log.error(e.getMessage(), e);
        return ProblemDetail.forStatusAndDetail(e.getHttpStatus(), e.getMessage());
    }

}
