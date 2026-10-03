package io.osvaldas.risk.domain.validators

import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS
import static org.springframework.http.HttpStatus.BAD_GATEWAY

import io.osvaldas.api.exceptions.ValidationRuleException.LoanLimitException
import io.osvaldas.api.loans.TodayTakenLoansCount
import io.osvaldas.risk.AbstractSpec
import io.osvaldas.risk.domain.validation.BackOfficeClient
import io.osvaldas.risk.domain.validation.BackOfficeResponseException
import io.osvaldas.risk.infra.configuration.PropertiesConfig
import io.osvaldas.risk.repositories.risk.RiskValidationTarget
import spock.lang.Subject

class LoanLimitValidatorSpec extends AbstractSpec {

    static final long LOAN_ID = 7

    PropertiesConfig config = Stub {
        loanLimitPerDay >> 2
    }

    BackOfficeClient client = Stub()

    @Subject
    LoanLimitValidator loanLimitValidator = new LoanLimitValidator(config, client)

    void 'should pass validation when #count earlier loans are active today and limit is 2'() {
        given:
            client.getLoansTakenTodayCount(clientId, LOAN_ID) >> new TodayTakenLoansCount(count)
        when:
            loanLimitValidator.validate(new RiskValidationTarget(clientId: clientId, loanId: LOAN_ID))
        then:
            notThrown(LoanLimitException)
        where:
            count << [0, 1]
    }

    void 'should throw exception when #count earlier loans are active today and limit is 2'() {
        given:
            client.getLoansTakenTodayCount(clientId, LOAN_ID) >> new TodayTakenLoansCount(count)
        when:
            loanLimitValidator.validate(new RiskValidationTarget(clientId: clientId, loanId: LOAN_ID))
        then:
            LoanLimitException e = thrown()
            e.message == LOAN_LIMIT_EXCEEDS
        where:
            count << [2, 6]
    }

    void 'should fail when back-office returns no count'() {
        given:
            client.getLoansTakenTodayCount(clientId, LOAN_ID) >> null
        when:
            loanLimitValidator.validate(new RiskValidationTarget(clientId: clientId, loanId: LOAN_ID))
        then:
            BackOfficeResponseException e = thrown()
            e.message == LoanLimitValidator.NO_LOANS_COUNT
            e.httpStatus == BAD_GATEWAY
    }

}
