package io.osvaldas.backoffice.infra.exception;

import static io.osvaldas.api.util.ExceptionMessages.CLIENT_ALREADY_EXIST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import feign.FeignException;
import io.osvaldas.api.exceptions.ApiRequestException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    static final String PERSONAL_CODE_CONSTRAINT = "uk_client_personal_code";

    static final String CONCURRENT_MODIFICATION = "The resource was modified by another request. Reload it and try again.";

    static final String DATA_CONFLICT = "The request conflicts with existing data.";

    static final String DOWNSTREAM_ERROR = "A downstream service failed to process the request. Try again later.";

    static final String DOWNSTREAM_UNAVAILABLE = "A downstream service is unavailable. Try again later.";

    @ExceptionHandler(ApiRequestException.class)
    public ProblemDetail handleApiRequestException(ApiRequestException e) {
        return problem(e.getHttpStatus(), e.getMessage(), e);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockingFailure(OptimisticLockingFailureException e) {
        return problem(CONFLICT, CONCURRENT_MODIFICATION, e);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return problem(CONFLICT, isPersonalCodeViolation(e) ? CLIENT_ALREADY_EXIST : DATA_CONFLICT, e);
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(FeignException e) {
        return e.status() > 0
            ? problem(BAD_GATEWAY, DOWNSTREAM_ERROR, e)
            : problem(SERVICE_UNAVAILABLE, DOWNSTREAM_UNAVAILABLE, e);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<String> errors = e.getBindingResult().getAllErrors().stream()
            .map(ApiExceptionHandler::describe)
            .toList();
        return withErrors(super.handleMethodArgumentNotValid(e, headers, status, request), errors);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException e,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        List<String> errors = e.getParameterValidationResults().stream()
            .flatMap(result -> result.getResolvableErrors().stream()
                .map(error -> result.getMethodParameter().getParameterName() + ": " + error.getDefaultMessage()))
            .toList();
        return withErrors(super.handleHandlerMethodValidationException(e, headers, status, request), errors);
    }

    private static ResponseEntity<Object> withErrors(ResponseEntity<Object> response, List<String> errors) {
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            problem.setProperty("errors", errors);
        }
        return response;
    }

    private static String describe(ObjectError error) {
        return error instanceof FieldError fieldError
            ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
            : error.getDefaultMessage();
    }

    private static ProblemDetail problem(HttpStatus status, String detail, Exception e) {
        if (status.is4xxClientError()) {
            log.warn("Request failed with status {}: {} ({})", status.value(), detail, e.getClass().getSimpleName());
        } else {
            log.error("Request failed with status {}: {}", status.value(), detail, e);
        }
        return ProblemDetail.forStatusAndDetail(status, detail);
    }

    private static boolean isPersonalCodeViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains(PERSONAL_CODE_CONSTRAINT);
    }

}
