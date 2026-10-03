package io.osvaldas.risk.domain.validators

import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.infra.configuration.PropertiesConfig
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class LoanLimitValidatorSpec extends AbstractSpec {

    PropertiesConfig config = Stub {
        loanLimitPerDay >> 2
    }

    @Subject
    LoanLimitValidator loanLimitValidator = new LoanLimitValidator(config)

    void 'should pass validation when #count other loans taken today and limit is 2'() {
        when:
            loanLimitValidator.validate(new RiskValidationTarget(loansTakenToday: count))
        then:
            notThrown(LoanLimitException)
        where:
            count << [0, 1]
    }

    void 'should throw exception when #count other loans taken today and limit is 2'() {
        when:
            loanLimitValidator.validate(new RiskValidationTarget(loansTakenToday: count))
        then:
            LoanLimitException e = thrown()
            e.message == LOAN_LIMIT_EXCEEDS
        where:
            count << [2, 6]
    }

}
