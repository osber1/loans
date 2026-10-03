package io.osvaldas.backoffice.infra.exception;

import static io.osvaldas.api.util.ExceptionMessages.CLIENT_ALREADY_EXIST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;
import io.osvaldas.api.exceptions.ApiRequestException;
import lombok.extern.slf4j.Slf4j;

/**
 * Translates exceptions into RFC 9457 problem details.
 * <ul>
 *     <li>{@link ApiRequestException} - the status carried by the exception.</li>
 *     <li>{@link OptimisticLockingFailureException} - 409, the resource was changed concurrently (stale version).</li>
 *     <li>{@link DataIntegrityViolationException} - 409, e.g. a duplicate personal code that slipped past the
 *     existence check because of a concurrent registration.</li>
 *     <li>{@link FeignException} - 502 when a downstream service answered with an error, 503 when it could not be
 *     reached at all (no HTTP status, e.g. connection refused or timeout).</li>
 * </ul>
 * Client errors (4xx) are logged at WARN without a stack trace; server errors (5xx) at ERROR with the stack trace.
 */
@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

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
