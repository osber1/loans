package io.osvaldas.loans.domain.loans

import static java.lang.String.format
import static java.util.Collections.emptySet
import static java.util.Collections.singletonList
import static java.util.Optional.empty
import static java.util.Optional.of

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

import io.osvaldas.loans.domain.AbstractServiceSpec
import io.osvaldas.loans.domain.clients.ClientService
import io.osvaldas.loans.domain.exceptions.NotFoundException
import io.osvaldas.loans.domain.loans.validators.RiskValidator
import io.osvaldas.loans.domain.loans.validators.TimeAndAmountValidator
import io.osvaldas.loans.domain.loans.validators.TimeAndAmountValidator.AmountException
import io.osvaldas.loans.domain.util.TimeUtils
import io.osvaldas.loans.infra.configuration.PropertiesConfig
import io.osvaldas.loans.repositories.LoanRepository
import io.osvaldas.loans.repositories.entities.Loan

class LoanServiceSpec extends AbstractServiceSpec {

//    @Shared
//    LoanPostpone firstPostpone = buildExtension(15.00, date.plusWeeks(1))
//
//    @Shared
//    LoanPostpone secondPostpone = buildExtension(22.50, date.plusWeeks(2))
//
//    @Shared
//    Loan successfulLoan = buildLoan(100.00)
//
//    @Shared
//    Loan loanWithPostpone = buildLoanWithPostpone(buildLoan(100.00), firstPostpone)

    ClientService clientService = Stub()

    TimeUtils timeUtils = Stub {
        currentDateTime >> date
        hourOfDay >> 10
    }

    PropertiesConfig config = Stub {
        requestsFromSameIpLimit >> 3
        maxAmount >> 100.00
        forbiddenHourFrom >> 0
        forbiddenHourTo >> 6
    }

    LoanRepository loanRepository = Mock()

    TimeAndAmountValidator timeAndAmountValidator = new TimeAndAmountValidator(riskMessage, amountExceedsMessage, config, timeUtils)

    RiskValidator validator = new RiskValidator(singletonList(timeAndAmountValidator))

    RedisTemplate<String, Integer> redisTemplate = Stub {
        opsForValue() >> valueOperations
    }

    ValueOperations valueOperations = Stub {
        valueOperations.get(_ as String) >> 2
    }

    LoanService loanService = new LoanService(clientService, loanRepository, config, timeUtils, validator, loanErrorMessage)

    void 'should save loan'() {
        when:
            loanService.save(loan)
        then:
            1 * loanRepository.save(loan) >> loan
    }

    void 'should return loans list when there are loans'() {
        when:
            Collection loans = loanService.getLoans(clientId)
        then:
            loans.size() == 1
        and:
            loans == Set.of(loan)
        and:
            clientService.getClient(clientId) >> clientWithLoans
    }

    void 'should return empty list when there are no loans'() {
        when:
            Collection loans = loanService.getLoans(clientId)
        then:
            loans == emptySet()
        and:
            clientService.getClient(clientId) >> clientWithId
    }

    void 'should return loan when it exists'() {
        when:
            Loan loan = loanService.getLoan(loanId)
        then:
            loan.id == loanId
        and:
            1 * loanRepository.findById(loanId) >> of(loan)
    }

    void 'should throw exception when trying to get non existing loan'() {
        when:
            loanService.getLoan(loanId)
        then:
            NotFoundException e = thrown()
            e.message == format(loanErrorMessage, loanId)
        and:
            1 * loanRepository.findById(loanId) >> empty()
    }

    void 'should fail when amount limit is exceeded'() {
        when:
            loanService.takeLoan(buildLoan(1000.00), clientId)
        then:
            AmountException exception = thrown()
            exception.message == amountExceedsMessage
    }

//    void 'should fail when max amount and forbidden time'() {
//        given:
//            redisTemplate.opsForValue() >> valueOperations
//            valueOperations.get(_ as String) >> 2
//            config.requestsFromSameIpLimit >> 3
//            config.maxAmount >> 100.00
//            config.forbiddenHourFrom >> 0
//            config.forbiddenHourTo >> 6
//            timeUtils.hourOfDay >> 4
//        when:
//            clientService.takeLoan(successfulLoan, validUserId)
//        then:
//            TimeException exception = thrown()
//            exception.message == 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'
//    }
//
//    void 'should be successful when the user is new'() {
//        given:
//            redisTemplate.opsForValue() >> valueOperations
//            valueOperations.get(_ as String) >> 2
//
//            clientRepository.save(_ as Client) >> clientFromDatabase
//            clientRepository.findByPersonalCode(_ as Long) >> Optional.empty()
//            clientRepository.findById(_ as String) >> Optional.of(clientFromDatabase)
//        when:
//            Loan loanResponse = clientService.takeLoan(successfulLoan, validUserId)
//        then:
//            successfulLoan == loanResponse
//    }
//
//    void 'should be successful when user is not new'() {
//        given:
//            redisTemplate.opsForValue() >> valueOperations
//            valueOperations.get(_ as String) >> 2
//            config.requestsFromSameIpLimit >> 10
//            config.maxAmount >> 100.00
//            config.forbiddenHourFrom >> 0
//            config.forbiddenHourTo >> 6
//            timeUtils.currentDateTime >> date
//            timeUtils.hourOfDay >> 10
//            clientRepository.save(_ as Client) >> clientFromDatabase
//            clientRepository.findById(_ as String) >> Optional.of(clientFromDatabase)
//            clientRepository.findByPersonalCode(_ as Long) >> Optional.of(clientFromDatabase)
//        when:
//            Loan loanResponse = clientService.takeLoan(successfulLoan, validUserId)
//        then:
//            successfulLoan == loanResponse
//    }
//
//    void 'should throw exception when getting history and client not exist'() {
//        given:
//            clientRepository.findById(_ as String) >> Optional.empty()
//        when:
//            clientService.getClientHistory(validUserId)
//        then:
//            NotFoundException e = thrown()
//            e.message == 'Client with id TEST-CORRECT-ID does not exist.'
//    }
//
//    void 'should return history when client exists'() {
//        given:
//            clientRepository.findById(_ as String) >> Optional.of(clientFromDatabase)
//        when:
//            Set<Loan> clientHistory = clientService.getClientHistory(validUserId)
//        then:
//            clientFromDatabase.loans == clientHistory
//    }
//
//    void 'should fail loan postpone when loan not exist'() {
//        given:
//            loanRepository.findById(_ as Integer) >> Optional.empty()
//        when:
//            clientService.postponeLoan(1)
//        then:
//            NotFoundException e = thrown()
//            e.message == 'Loan with id 1 does not exist.'
//    }
//
//    void 'should postpone loan when it is first postpone'() {
//        given:
//            loanRepository.findById(_ as Long) >> Optional.of(successfulLoan)
//            loanRepository.save(_ as Loan) >> loanWithPostpone
//        when:
//            LoanPostpone loanPostponeResponse = clientService.postponeLoan(1)
//        then:
//            firstPostpone == loanPostponeResponse
//    }
//
//    void 'should postpone loan when it is not first postpone'() {
//        given:
//            loanRepository.findById(_ as Long) >> Optional.of(loanWithPostpone)
//            loanRepository.save(_ as Loan) >> buildLoanWithPostpones(buildLoan(100.00), firstPostpone, secondPostpone)
//        when:
//            LoanPostpone loanPostponeResponse = clientService.postponeLoan(1)
//        then:
//            secondPostpone == loanPostponeResponse
//    }
}
