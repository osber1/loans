package io.osvaldas.loans.domain

import static java.util.Collections.singletonList

import java.time.ZoneId
import java.time.ZonedDateTime

import io.osvaldas.loans.repositories.entities.Client
import io.osvaldas.loans.repositories.entities.Loan
import io.osvaldas.loans.repositories.entities.LoanPostpone
import spock.lang.Shared
import spock.lang.Specification

class AbstractServiceSpec extends Specification {

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
    String clientErrorMessage = "Client with id ${clientId} does not exist."

    @Shared
    String loanErrorMessage = "Loan with id ${loanId} does not exist."

    @Shared
    String riskMessage = 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'

    @Shared
    String amountExceedsMessage = 'The amount you are trying to borrow exceeds the max amount!'

    //    RedisTemplate<String, Integer> redisTemplate = Stub {
//        opsForValue() >> valueOperations
//    }
//
//    ValueOperations valueOperations = Stub {
//        valueOperations.get(_ as String) >> 2
//    }

//    @Shared
//    LoanPostpone firstPostpone = buildExtension(15.00, date.plusWeeks(1))
//
//    @Shared
//    LoanPostpone secondPostpone = buildExtension(22.50, date.plusWeeks(2))
//
//    @Shared
//    Loan loanWithPostpone = buildLoanWithPostpone(buildLoan(100.00), firstPostpone)

    @Shared
    Loan loan = buildLoan(100.0)

    @Shared
    Client clientWithoutId = buildClient('', new HashSet<Loan>())

    @Shared
    Client clientWithId = buildClient(clientId, new HashSet<Loan>())

    @Shared
    Client clientWithLoan = buildClient(clientId, Set.of(loan))

    LoanPostpone buildExtension(BigDecimal newRate, ZonedDateTime newDate) {
        new LoanPostpone().tap {
            id = 1
            newInterestRate = newRate
            newReturnDate = newDate
        }
    }

    Loan buildLoanWithPostpone(Loan loanRequest, LoanPostpone postpone) {
        List<LoanPostpone> list = singletonList(postpone)
        loanRequest.with { loanPostpones = new HashSet<>(list) }
        return loanRequest
    }

    Loan buildLoanWithPostpones(Loan loanRequest, LoanPostpone postpone, LoanPostpone secondPostpone) {
        List<LoanPostpone> list = Arrays.asList(postpone, secondPostpone)
        loanRequest.with { loanPostpones = new HashSet<>(list) }
        return loanRequest
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