package io.osvaldas.backoffice

import static io.osvaldas.backoffice.repositories.entities.Status.ACTIVE
import static io.osvaldas.backoffice.repositories.entities.Status.REGISTERED
import static java.lang.Long.parseLong
import static java.util.Set.of

import java.time.ZoneId
import java.time.ZonedDateTime

import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import io.osvaldas.backoffice.repositories.entities.LoanPostpone
import io.osvaldas.backoffice.repositories.entities.Status
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractSpec extends Specification {

    @Shared
    int loanTermInMonths = 12

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
    long loanId = 1

    @Shared
    String clientErrorMessage = "Client with id ${clientId} does not exist."

    @Shared
    String clientAlreadyExistErrorMessage = 'Client with personal code already exists.'

    @Shared
    String loanErrorMessage = "Loan with id ${loanId} does not exist."

    @Shared
    @SuppressWarnings('LineLength')
    String riskMessage = 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'

    @Shared
    String amountExceedsMessage = 'The amount you are trying to borrow exceeds the max amount!'

    @Shared
    String ipExceedsMessage = 'Too many requests from the same ip per day.'

    @Shared
    String clientNotActiveMessage = 'Client is not active.'

    @Shared
    LoanPostpone firstPostpone = buildPostpone(1, 15.00, date.plusMonths(loanTermInMonths).plusWeeks(1))

    @Shared
    LoanPostpone secondPostpone = buildPostpone(2, 22.50, date.plusMonths(loanTermInMonths).plusWeeks(2))

    @Shared
    Loan loan = buildLoan(100.0)

    @Shared
    Client registeredClientWithoutId = buildClient('', [] as Set, REGISTERED)

    @Shared
    Client registeredClientWithId = buildClient(clientId, [] as Set, REGISTERED)

    @Shared
    Client registeredClientWithLoan = buildClient(clientId, of(loan), REGISTERED)

    @Shared
    Client activeClientWithId = buildClient(clientId, [] as Set, ACTIVE)

    @Shared
    Client activeClientWithLoan = buildClient(clientId, of(loan), ACTIVE)

    LoanPostpone buildPostpone(long loanId, BigDecimal newRate, ZonedDateTime newDate) {
        new LoanPostpone().tap {
            id = loanId
            interestRate = newRate
            returnDate = newDate
        }
    }

    Loan buildLoan(BigDecimal loanAmount) {
        new Loan().tap {
            id = loanId
            amount = loanAmount
            termInMonths = loanTermInMonths
            interestRate = 10.0
            returnDate = date.plusMonths(loanTermInMonths)
        }
    }

    Client buildClient(String clientId, Set<Loan> clientLoans, Status clientStatus) {
        new Client().tap {
            id = clientId
            firstName = name
            lastName = surname
            email = clientEmail
            status = clientStatus
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
