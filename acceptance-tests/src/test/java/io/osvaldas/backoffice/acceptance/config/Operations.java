package io.osvaldas.backoffice.acceptance.config;

import java.math.BigDecimal;

import io.osvaldas.api.clients.ClientRegisterRequest;
import io.osvaldas.api.loans.LoanRequest;
import net.datafaker.Faker;

public class Operations {

    private static final Faker FAKER = new Faker();

    static LoanRequest buildLoanRequest(BigDecimal loanAmount) {
        return new LoanRequest(loanAmount, 12);
    }

    static ClientRegisterRequest buildRegisterClientRequest() {
        return new ClientRegisterRequest(
            FAKER.name().firstName(),
            FAKER.name().lastName(),
            FAKER.expression("#{bothify '???????????####@gmail.com'}"),
            FAKER.expression("370#{numerify '########'}"),
            FAKER.expression("#{numerify '###########'}")
        );
    }

}
