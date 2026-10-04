package io.osvaldas.risk.domain.validators

import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS

import io.osvaldas.api.risk.validation.RiskRejectionReason
import io.osvaldas.api.risk.validation.RiskValidationRequest
import io.osvaldas.risk.domain.validation.Rejection
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.infra.configuration.PropertiesConfig
import spock.lang.Subject

class LoanLimitValidatorSpec extends AbstractSpec {

    PropertiesConfig config = Stub {
        loanLimitPerDay >> 2
    }

    @Subject
    LoanLimitValidator loanLimitValidator = new LoanLimitValidator(config)

    void 'should pass validation when #count other loans taken today and limit is 2'() {
        expect:
            loanLimitValidator.check(request(count)).empty
        where:
            count << [0, 1]
    }

    void 'should reject when #count other loans taken today and limit is 2'() {
        expect:
            loanLimitValidator.check(request(count)) ==
                Optional.of(new Rejection(RiskRejectionReason.LOAN_LIMIT_EXCEEDS, LOAN_LIMIT_EXCEEDS))
        where:
            count << [2, 6]
    }

    private static RiskValidationRequest request(long loansTakenToday) {
        new RiskValidationRequest(1L, 'clientId', 50.0, loansTakenToday, null)
    }

}
