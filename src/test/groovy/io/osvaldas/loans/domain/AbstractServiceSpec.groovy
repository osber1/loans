package io.osvaldas.loans.domain

import static java.util.Arrays.asList

import java.time.ZoneId
import java.time.ZonedDateTime

import io.osvaldas.loans.repositories.entities.Client
import io.osvaldas.loans.repositories.entities.Loan
import io.osvaldas.loans.repositories.entities.LoanPostpone
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractServiceSpec extends Specification {

    @Shared
    long existingPersonalCode = 12345678910

    @Shared
    String clientId = 'userId'

    @Shared
    long loanId = 1

    @Shared
    String timeZone = 'Europe/Vilnius'

    @Shared
    ZonedDateTime date = generateDate()

    @Shared
    String clientErrorMessage = 'Client with id ${clientId} does not exist.'

    @Shared
    String loanErrorMessage = 'Loan with id ${loanId} does not exist.'

    @Shared
    String riskMessage = 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'

    @Shared
    String amountExceedsMessage = 'The amount you are trying to borrow exceeds the max amount!'

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
            interestRate = 10
            returnDate = date.plusYears(1)
        }
    }

    Client buildClient(String clientId, Set<Loan> clientLoans) {
        return new Client().tap {
            id = clientId
            firstName = 'Testas'
            lastName = 'Testaitis'
            personalCode = existingPersonalCode
            loans = clientLoans
        }
    }

    ZonedDateTime generateDate() {
        return ZonedDateTime.of(
            2020,
            1,
            1,
            1,
            1,
            1,
            1,
            ZoneId.of(timeZone))
    }
}