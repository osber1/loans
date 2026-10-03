package io.osvaldas.risk

import static io.osvaldas.api.util.ExceptionMessages.RISK_TOO_HIGH

import spock.lang.Shared
import spock.lang.Specification

class AbstractSpec extends Specification {

    @Shared
    String clientId = 'clientId'

    @Shared
    String riskTooHigh = RISK_TOO_HIGH.formatted('00:00', '06:00')

}
