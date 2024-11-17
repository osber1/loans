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
import spock.lang.Specification

abstract class AbstractSpec extends Specification {

    static final int LOAN_TERM_IN_MONTHS = 12

    static final ZonedDateTime DATE = generateDate()

    static final String CLIENT_ID = 'clientId'

    static final String NAME = 'Name'

    static final String SURNAME = 'Surname'

    static final String CLIENT_PERSONAL_CODE = '12345678910'

    static final String CLIENT_EMAIL = 'test@mail.com'

    static final String CLIENT_PHONE_NUMBER = '37062514361'

    static final long LOAN_ID = 1

    static final String CLIENT_NOT_FOUND = "Client with id ${CLIENT_ID} does not exist."

    static final String CLIENT_ALREADY_EXIST = 'Client with personal code already exists.'

    static final String LOAN_NOT_FOUND = 'Loan with id %s does not exist.'

    static final String RISK_TOO_HIGH = 'Risk is too high, because you are trying ' +
        'to get loan between 00:00 and 6:00 and you want to borrow the max amount!'

    static final String AMOUNT_EXCEEDS = 'The amount you are trying to borrow exceeds the max amount!'

    static final String LOAN_LIMIT_EXCEEDS = 'Too many loans taken in a single day.'

    static final String CLIENT_NOT_ACTIVE = 'Client is not active.'

    static final String LOAN_NOT_OPEN = "Loan with id ${LOAN_ID} is not open."

    LoanPostpone firstPostpone = buildPostpone(1, 15.00, DATE.plusMonths(LOAN_TERM_IN_MONTHS).plusWeeks(1))

    LoanPostpone secondPostpone = buildPostpone(2, 22.50, DATE.plusMonths(LOAN_TERM_IN_MONTHS).plusWeeks(2))

    Loan loan = buildLoan(100.0)

    Client registeredClientWithoutId = buildClient('', [] as Set, REGISTERED)

    Client registeredClientWithId = buildClient(CLIENT_ID, [] as Set, REGISTERED)

    Client registeredClientWithLoan = buildClient(CLIENT_ID, [loan] as Set, REGISTERED)

    Client activeClientWithId = buildClient(CLIENT_ID, [] as Set, ACTIVE)

    Client activeClientWithLoan = buildClient(CLIENT_ID, [loan] as Set, ACTIVE)

    static ZonedDateTime generateDate() {
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

    LoanPostpone buildPostpone(long loanId, BigDecimal newRate, ZonedDateTime newDate) {
        new LoanPostpone().tap {
            id = loanId
            interestRate = newRate
            returnDate = newDate
        }
    }

    Loan buildLoan(BigDecimal loanAmount, io.osvaldas.api.loans.Status loanStatus = OPEN) {
        new Loan().tap {
            id = LOAN_ID
            amount = loanAmount
            status = loanStatus
            termInMonths = LOAN_TERM_IN_MONTHS
            interestRate = 10.0
            returnDate = DATE.plusMonths(LOAN_TERM_IN_MONTHS)
        }
    }

    Loan buildLoanWithoutId(BigDecimal loanAmount, io.osvaldas.api.loans.Status loanStatus = OPEN) {
        new Loan().tap {
            amount = loanAmount
            status = loanStatus
            termInMonths = LOAN_TERM_IN_MONTHS
            interestRate = 10.0
            returnDate = DATE.plusMonths(LOAN_TERM_IN_MONTHS)
        }
    }

    Client buildClient(String clientId, Set<Loan> clientLoans, Status clientStatus) {
        Client newClient = new Client().tap {
            id = clientId
            firstName = NAME
            lastName = SURNAME
            email = CLIENT_EMAIL
            status = clientStatus
            phoneNumber = CLIENT_PHONE_NUMBER
            personalCode = CLIENT_PERSONAL_CODE
            loans = clientLoans
        }
        clientLoans.each { it.client = newClient }
        newClient
    }

}
