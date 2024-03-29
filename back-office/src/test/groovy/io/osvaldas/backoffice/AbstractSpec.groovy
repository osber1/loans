package io.osvaldas.backoffice

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.clients.Status.REGISTERED
import static io.osvaldas.api.loans.Status.OPEN

import java.time.ZoneId
import java.time.ZonedDateTime

import io.osvaldas.api.clients.Status
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import io.osvaldas.backoffice.repositories.entities.LoanPostpone
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractSpec extends Specification {

    @Shared
    int loanTermInMonths = 12

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
    String clientPhoneNumber = '37062514361'

    @Shared
    long loanId = 1

    @Shared
    String clientNotFound = "Client with id ${clientId} does not exist."

    @Shared
    String clientAlreadyExist = 'Client with personal code already exists.'

    @Shared
    String loanNotFound = 'Loan with id %s does not exist.'

    @Shared
    String riskTooHigh = 'Risk is too high, because you are trying ' +
        'to get loan between 00:00 and 6:00 and you want to borrow the max amount!'

    @Shared
    String amountExceeds = 'The amount you are trying to borrow exceeds the max amount!'

    @Shared
    String loanLimitExceeds = 'Too many loans taken in a single day.'

    @Shared
    String clientNotActive = 'Client is not active.'

    @Shared
    String loanNotOpen = "Loan with id ${loanId} is not open."

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
    Client registeredClientWithLoan = buildClient(clientId, Set.of(loan), REGISTERED)

    @Shared
    Client activeClientWithId = buildClient(clientId, [] as Set, ACTIVE)

    @Shared
    Client activeClientWithLoan = buildClient(clientId, Set.of(loan), ACTIVE)

    LoanPostpone buildPostpone(long loanId, BigDecimal newRate, ZonedDateTime newDate) {
        new LoanPostpone().tap {
            id = loanId
            interestRate = newRate
            returnDate = newDate
        }
    }

    Loan buildLoan(BigDecimal loanAmount, io.osvaldas.api.loans.Status loanStatus = OPEN) {
        new Loan().tap {
            id = loanId
            amount = loanAmount
            status = loanStatus
            termInMonths = loanTermInMonths
            interestRate = 10.0
            returnDate = date.plusMonths(loanTermInMonths)
        }
    }

    Client buildClient(String clientId, Set<Loan> clientLoans, Status clientStatus) {
        Client newClient = new Client().tap {
            id = clientId
            firstName = name
            lastName = surname
            email = clientEmail
            status = clientStatus
            phoneNumber = clientPhoneNumber
            personalCode = clientPersonalCode
            loans = clientLoans
        }
        clientLoans.each { it.client = newClient }
        newClient
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
            ZoneId.of('UTC'))
    }

}
