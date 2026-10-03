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
        loanLimitPerDay >> 2
    }

    BackOfficeClient client = Stub()

    @Subject
    LoanLimitValidator loanLimitValidator = new LoanLimitValidator(config, client)

    void 'should pass validation when #count other loans taken today and limit is 2'() {
        given:
            client.getLoansTakenTodayCount(clientId) >> new TodayTakenLoansCount(count)
        when:
            loanLimitValidator.validate(new RiskValidationTarget(clientId: clientId))
        then:
            notThrown(LoanLimitException)
        where:
            count << [0, 1]
    }

    void 'should throw exception when #count other loans taken today and limit is 2'() {
        given:
            client.getLoansTakenTodayCount(clientId) >> new TodayTakenLoansCount(count)
        when:
            loanLimitValidator.validate(new RiskValidationTarget(clientId: clientId))
        then:
            LoanLimitException e = thrown()
            e.message == loanLimitExceeds
        where:
            count << [2, 6]
    }

    void 'should pass validation when back-office returns no count'() {
        given:
            client.getLoansTakenTodayCount(clientId) >> null
        when:
            loanLimitValidator.validate(new RiskValidationTarget(clientId: clientId))
        then:
            notThrown(LoanLimitException)
    }

}
