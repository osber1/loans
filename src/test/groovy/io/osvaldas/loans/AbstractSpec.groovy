package io.osvaldas.loans

import static java.lang.Long.parseLong
import static java.util.Arrays.asList

import java.time.ZoneId
import java.time.ZonedDateTime

import io.osvaldas.loans.repositories.entities.Client
import io.osvaldas.loans.repositories.entities.Loan
import io.osvaldas.loans.repositories.entities.LoanPostpone
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractSpec extends Specification {

    @Shared
    String timeZone = 'Europe/Vilnius'

    @Shared
    ZonedDateTime date = generateDate()

    @Shared
    String clientId = 'clientId'

    @Shared
    String name = 'Name'

    @Shared
    String surname = 'Surname'

    @Shared
    String clientPersonalCode = '12345678910'

    @Shared
    String clientEmail = 'test@mail.com'

    @Shared
    String clientPhoneNumber = '+37062514361'

    @Shared
    long existingPersonalCode = 12345678910

    @Shared
    long loanId = 1

    @Shared
    String clientErrorMessage = "Client with id ${clientId} does not exist."

    @Shared
    String loanErrorMessage = "Loan with id ${loanId} does not exist."

    @Shared
    String riskMessage = 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'

    @Shared
    String amountExceedsMessage = 'The amount you are trying to borrow exceeds the max amount!'

    @Shared
    String ipExceedsMessage = 'Too many requests from the same ip per day.'

    @Shared
    LoanPostpone firstPostpone = buildPostpone(15.00, date.plusWeeks(1))

    @Shared
    LoanPostpone secondPostpone = buildPostpone(22.50, date.plusWeeks(2))

    @Shared
    Loan loan = buildLoan(100.0)

    @Shared
    Client clientWithoutId = buildClient('', new HashSet<Loan>())

    @Shared
    Client clientWithId = buildClient(clientId, new HashSet<Loan>())

    @Shared
    Client clientWithLoan = buildClient(clientId, Set.of(loan))

    LoanPostpone buildPostpone(BigDecimal newRate, ZonedDateTime newDate) {
        new LoanPostpone().tap {
            id = 1
            interestRate = newRate
            returnDate = newDate
        }
    }

    Loan addPostponeToLoan(Loan loan, LoanPostpone... postpones) {
        loan.with { loanPostpones = new HashSet<>(asList(postpones)) }
        return loan
    }

    Loan buildLoan(BigDecimal loanAmount) {
        new Loan().tap {
            id = loanId
            amount = loanAmount
            termInMonths = 12
            interestRate = 10.0
            returnDate = date.plusYears(1)
        }
    }

    Client buildClient(String clientId, Set<Loan> clientLoans) {
        new Client().tap {
            id = clientId
            firstName = name
            lastName = surname
            email = clientEmail
            phoneNumber = clientPhoneNumber
            personalCode = parseLong(clientPersonalCode)
            loans = clientLoans
        }
    }

    ZonedDateTime generateDate() {
        ZonedDateTime.of(
            2020,
            1,
            5,
            10,
            0,
            0,
            0,
            ZoneId.of(timeZone))
    }
}