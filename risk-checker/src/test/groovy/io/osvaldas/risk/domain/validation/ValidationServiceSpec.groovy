package io.osvaldas.risk.domain.validation

import io.osvaldas.api.exceptions.ValidationRuleException.AmountException
import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.api.loans.Status
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Shared
import spock.lang.Subject

class ValidationServiceSpec extends AbstractSpec {

    @Shared
    long loanId = 1

    @Shared
    BigDecimal amount = 55.5

    @Shared
    RiskValidationRequest request = new RiskValidationRequest(loanId, clientId)

    Validator validator = Mock()

    BackOfficeClient backOfficeClient = Stub {
        getLoan(loanId) >> new LoanResponse(loanId, amount, 10.0, 12, Status.NOT_EVALUATED, null, null, [] as Set)
    }

    @Subject
    ValidationService validationService = new ValidationService(validator, backOfficeClient)

    void 'should return successful validation when checking risk'() {
        when:
            RiskValidationResponse response = validationService.validate(request)
        then:
            response.success()
        and:
            1 * validator.validate(new RiskValidationTarget(amount, clientId))
    }

    void 'should return failed validation when validation rule rejects loan'() {
        when:
            RiskValidationResponse response = validationService.validate(request)
        then:
            1 * validator.validate(_ as RiskValidationTarget) >> { throw new AmountException(amountExceeds) }
        and:
            !response.success()
            response.message() == amountExceeds
    }

    void 'should propagate infrastructure failures instead of rejecting loan'() {
        given:
            IllegalStateException failure = new IllegalStateException('back-office is down')
        when:
            validationService.validate(request)
        then:
            1 * validator.validate(_ as RiskValidationTarget) >> { throw failure }
        and:
            IllegalStateException e = thrown()
            e.is(failure)
    }

}
