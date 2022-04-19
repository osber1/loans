package io.osvaldas.fraud.domain.validators

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException
import io.osvaldas.api.loans.TodayTakenLoansCount
import io.osvaldas.fraud.AbstractSpec
import io.osvaldas.fraud.domain.validation.BackOfficeClient
import io.osvaldas.fraud.infra.configuration.PropertiesConfig
import io.osvaldas.fraud.repositories.risk.RiskValidationTarget
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
