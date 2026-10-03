package io.osvaldas.risk.infra.exception;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import feign.FeignException;
import feign.RetryableException;
import io.osvaldas.api.exceptions.ApiRequestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String BACK_OFFICE_UNAVAILABLE = "Back-office service is unavailable, try again later.";

    public static final String BACK_OFFICE_ERROR = "Back-office service returned an unexpected error.";

    @ExceptionHandler(ApiRequestException.class)
    public ProblemDetail handleApiRequestException(ApiRequestException e) {
        log.warn("Request failed: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(e.getHttpStatus(), e.getMessage());
    }

    @ExceptionHandler(RetryableException.class)
    public ProblemDetail handleRetryableException(RetryableException e) {
        log.error("Back-office is unreachable.", e);
        return ProblemDetail.forStatusAndDetail(SERVICE_UNAVAILABLE, BACK_OFFICE_UNAVAILABLE);
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(FeignException e) {
        log.error("Back-office call failed with status {}.", e.status(), e);
        if (e.status() == SERVICE_UNAVAILABLE.value()) {
            return ProblemDetail.forStatusAndDetail(SERVICE_UNAVAILABLE, BACK_OFFICE_UNAVAILABLE);
        }
        return ProblemDetail.forStatusAndDetail(BAD_GATEWAY, BACK_OFFICE_ERROR);
    }

}
