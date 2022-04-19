package io.osvaldas.fraud

import spock.lang.Shared
import spock.lang.Specification

class AbstractSpec extends Specification {

    @Shared
    String clientId = 'clientId'

    @Shared
    String loanLimitExceedsMessage = 'Too many loans taken in a single day.'

    @Shared
    String riskMessage = 'Risk is too high, because you are trying to get loan' +
        ' between 00:00 and 6:00 and you want to borrow the max amount!'

    @Shared
    String amountExceedsMessage = 'The amount you are trying to borrow exceeds the max amount!'

}
