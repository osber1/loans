package io.osvaldas.risk

import static io.osvaldas.api.util.ExceptionMessages.AMOUNT_EXCEEDS
import static io.osvaldas.api.util.ExceptionMessages.LOAN_LIMIT_EXCEEDS
import static io.osvaldas.api.util.ExceptionMessages.RISK_TOO_HIGH

import spock.lang.Shared
import spock.lang.Specification

class AbstractSpec extends Specification {

    @Shared
    String clientId = 'clientId'

    @Shared
    String loanLimitExceeds = LOAN_LIMIT_EXCEEDS

    @Shared
    String riskTooHigh = RISK_TOO_HIGH.formatted('00:00', '06:00')

    @Shared
    String amountExceeds = AMOUNT_EXCEEDS

}
