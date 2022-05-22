package io.osvaldas.risk.domain.validators

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException
import io.osvaldas.api.loans.TodayTakenLoansCount
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.domain.validation.BackOfficeClient
import io.osvaldas.risk.infra.configuration.PropertiesConfig
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class LoanLimitValidatorSpec extends AbstractSpec {

    PropertiesConfig config = Stub {
        loanLimitPerDay >> 1
    }

    BackOfficeClient client = Stub()

    @Subject
    LoanLimitValidator loanLimitValidator = new LoanLimitValidator(config, client)

    void setup() {
        loanLimitValidator.loanLimitExceedsMessage = loanLimitExceedsMessage
    }

    void 'should pass validation when loans per day limit not exceeded'() {
        given:
            client.getLoansTakenTodayCount(clientId) >> new TodayTakenLoansCount(1)
        when:
            loanLimitValidator.validate(new RiskValidationTarget(clientId: clientId))
        then:
            notThrown(LoanLimitException)
    }

    void 'should throw exception when loans per day limit exceeded'() {
        given:
            client.getLoansTakenTodayCount(clientId) >> new TodayTakenLoansCount(6)
        when:
            loanLimitValidator.validate(new RiskValidationTarget(clientId: clientId))
        then:
            LoanLimitException e = thrown()
            e.message == loanLimitExceedsMessage
    }

}
