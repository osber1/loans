package io.osvaldas.backoffice.acceptance

import io.osvaldas.api.clients.ClientRegisterRequest
import io.osvaldas.api.loans.LoanRequest
import net.datafaker.Faker
import spock.lang.Shared

class Operations {

    @Shared
    Faker faker = new Faker()

    static LoanRequest buildLoanRequest(BigDecimal loanAmount) {
        new LoanRequest().tap {
            amount = loanAmount
            termInMonths = 12
        }
    }

    ClientRegisterRequest buildRegisterClientRequest() {
        new ClientRegisterRequest().tap {
            firstName = faker.name().firstName()
            lastName = faker.name().lastName()
            personalCode = faker.expression("#{numerify '###########'}")
            email = faker.expression("#{bothify '???????????####@gmail.com'}")
            phoneNumber = faker.expression("370#{numerify '########'}")
        }
    }

}
