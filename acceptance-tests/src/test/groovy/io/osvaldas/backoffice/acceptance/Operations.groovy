package io.osvaldas.backoffice.acceptance

import com.github.javafaker.Faker
import com.github.javafaker.service.FakeValuesService
import com.github.javafaker.service.RandomService

import io.osvaldas.api.clients.ClientRegisterRequest
import io.osvaldas.api.loans.LoanRequest
import spock.lang.Shared

class Operations {

    @Shared
    Faker faker = new Faker()

    @Shared
    FakeValuesService fakeValuesService = new FakeValuesService(new Locale('lt-LT'), new RandomService())

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
            personalCode = fakeValuesService.numerify('###########')
            email = fakeValuesService.bothify('???????????####@gmail.com')
            phoneNumber = fakeValuesService.numerify('###########')
        }
    }

}
