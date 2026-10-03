package io.osvaldas.backoffice.infra.exception

import static org.springframework.http.HttpStatus.BAD_GATEWAY
import static org.springframework.http.HttpStatus.CONFLICT
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ProblemDetail
import org.springframework.orm.ObjectOptimisticLockingFailureException

import feign.FeignException
import io.osvaldas.api.exceptions.NotFoundException
import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.repositories.entities.Client
import spock.lang.Subject

class ApiExceptionHandlerSpec extends AbstractSpec {

    static final String DUPLICATE_PERSONAL_CODE = 'violates unique constraint "uk_client_personal_code"'

    static final String NOT_NULL_VIOLATION = 'null value in column "first_name" violates not-null constraint'

    @Subject
    ApiExceptionHandler handler = new ApiExceptionHandler()

    void 'should map api request exception to its status'() {
        when:
            ProblemDetail problem = handler.handleApiRequestException(new NotFoundException(CLIENT_NOT_FOUND))
        then:
            problem.status == NOT_FOUND.value()
            problem.detail == CLIENT_NOT_FOUND
    }

    void 'should map optimistic locking failure to conflict'() {
        when:
            ProblemDetail problem = handler.handleOptimisticLockingFailure(
                new ObjectOptimisticLockingFailureException(Client, CLIENT_ID))
        then:
            problem.status == CONFLICT.value()
            problem.detail == ApiExceptionHandler.CONCURRENT_MODIFICATION
    }

    void 'should map data integrity violation to conflict with detail #detail'() {
        when:
            ProblemDetail problem = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException('could not execute statement', new IllegalStateException(cause)))
        then:
            problem.status == CONFLICT.value()
            problem.detail == detail
        where:
            cause                   || detail
            DUPLICATE_PERSONAL_CODE || CLIENT_ALREADY_EXIST
            NOT_NULL_VIOLATION      || ApiExceptionHandler.DATA_CONFLICT
    }

    void 'should map feign exception with status #feignStatus to #expectedStatus'() {
        given:
            FeignException exception = Stub {
                status() >> feignStatus
            }
        when:
            ProblemDetail problem = handler.handleFeignException(exception)
        then:
            problem.status == expectedStatus.value()
        where:
            feignStatus || expectedStatus
            500         || BAD_GATEWAY
            400         || BAD_GATEWAY
            -1          || SERVICE_UNAVAILABLE
    }

}
