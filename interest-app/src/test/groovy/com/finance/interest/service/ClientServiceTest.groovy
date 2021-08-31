package com.finance.interest.service

import java.time.ZoneId
import java.time.ZonedDateTime

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations

import com.finance.interest.configuration.PropertiesConfig
import com.finance.interest.exception.NotFoundException
import com.finance.interest.interfaces.TimeUtils
import com.finance.interest.model.Client
import com.finance.interest.model.ClientDAO
import com.finance.interest.model.Loan
import com.finance.interest.model.LoanPostpone
import com.finance.interest.repository.ClientRepository
import com.finance.interest.repository.LoanRepository
import com.finance.interest.util.IpValidator
import com.finance.interest.util.IpValidator.IpException
import com.finance.interest.util.RiskValidator
import com.finance.interest.util.TimeAndAmountValidator
import com.finance.interest.util.TimeAndAmountValidator.AmountException
import com.finance.interest.util.TimeAndAmountValidator.TimeException

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class ClientServiceTest extends Specification {

    private static final String TIME_ZONE = 'Europe/Vilnius'

    public static final String IP = '0.0.0.0.0.1'

    private static final ZonedDateTime date = generateDate()

    public static final String CORRECT_USER_ID = 'TEST-CORRECT-ID'

    private TimeUtils timeUtils = Stub()

    private PropertiesConfig config = Stub()

    private LoanRepository loanRepository = Stub()

    private ClientRepository clientRepository = Stub()

    private RedisTemplate<String, Integer> redisTemplate = Stub()

    private ValueOperations valueOperations = Stub()

    private IpValidator ipValidator = new IpValidator(config, redisTemplate)

    private TimeAndAmountValidator timeAndAmountValidator = new TimeAndAmountValidator(config, timeUtils)

    private RiskValidator validator = new RiskValidator(Collections.singletonList(ipValidator), Collections.singletonList(timeAndAmountValidator))

    @Shared
    private LoanPostpone firstPostpone = buildLoanPostpone(15.00, date.plusWeeks(1))

    @Shared
    private LoanPostpone secondPostpone = buildLoanPostpone(22.50, date.plusWeeks(2))

    @Shared
    private Loan successfulLoan = buildLoan(100.00)

    @Shared
    private Loan loanWithPostpone = buildLoanWithPostpone(buildLoan(100.00), firstPostpone)

    @Shared
    private ClientDAO clientFromDatabase = buildClientResponse(successfulLoan)

    @Shared
    private Client successfulClient = buildClient(successfulLoan)

    @Subject
    ClientService clientService = new ClientService(clientRepository, loanRepository, config, validator, timeUtils)

    void 'should fail when ip limit is exceeded'() {
        given:
            redisTemplate.opsForValue() >> valueOperations
            valueOperations.get(_ as String) >> 3
            config.requestsFromSameIpLimit >> 3
        when:
            clientService.takeLoan(new Client(), IP)
        then:
            IpException exception = thrown()
            exception.message == 'Too many requests from the same ip per day.'
    }

    void 'should fail when amount limit is exceeded'() {
        given:
            redisTemplate.opsForValue() >> valueOperations
            valueOperations.get(_ as String) >> 2
            config.requestsFromSameIpLimit >> 3
            config.maxAmount >> 100.00
            config.forbiddenHourFrom >> 0
            config.forbiddenHourTo >> 6
            timeUtils.hourOfDay >> 10
        when:
            clientService.takeLoan(buildClient(buildLoan(1000.00)), IP)
        then:
            AmountException exception = thrown()
            exception.message == 'The amount you are trying to borrow exceeds the max amount!'
    }

    void 'should fail when max amount and forbidden time'() {
        given:
            redisTemplate.opsForValue() >> valueOperations
            valueOperations.get(_ as String) >> 2
            config.requestsFromSameIpLimit >> 3
            config.maxAmount >> 100.00
            config.forbiddenHourFrom >> 0
            config.forbiddenHourTo >> 6
            timeUtils.hourOfDay >> 4
        when:
            clientService.takeLoan(successfulClient, IP)
        then:
            TimeException exception = thrown()
            exception.message == 'Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!'
    }

    void 'should be successful when the user is new'() {
        given:
            redisTemplate.opsForValue() >> valueOperations
            valueOperations.get(_ as String) >> 2
            config.requestsFromSameIpLimit >> 10
            config.maxAmount >> 100.00
            config.forbiddenHourFrom >> 0
            config.forbiddenHourTo >> 6
            timeUtils.currentDateTime >> date
            timeUtils.hourOfDay >> 10
            clientRepository.save(_ as ClientDAO) >> clientFromDatabase
            clientRepository.findByPersonalCode(_ as Long) >> Optional.empty()
        when:
            Client clientResponse = clientService.takeLoan(successfulClient, IP)
        then:
            successfulClient == clientResponse
    }

    void 'should be successful when user is not new'() {
        given:
            redisTemplate.opsForValue() >> valueOperations
            valueOperations.get(_ as String) >> 2
            config.requestsFromSameIpLimit >> 10
            config.maxAmount >> 100.00
            config.forbiddenHourFrom >> 0
            config.forbiddenHourTo >> 6
            timeUtils.currentDateTime >> date
            timeUtils.hourOfDay >> 10
            clientRepository.save(_ as ClientDAO) >> clientFromDatabase
            clientRepository.findByPersonalCode(_ as Long) >> Optional.of(clientFromDatabase)
        when:
            Client clientResponse = clientService.takeLoan(successfulClient, IP)
        then:
            clientResponse == successfulClient
    }

    void 'should throw exception when getting history and client not exist'() {
        given:
            clientRepository.findById(_ as String) >> Optional.empty()
        when:
            clientService.getClientHistory(CORRECT_USER_ID)
        then:
            NotFoundException e = thrown()
            e.message == 'Client with id TEST-CORRECT-ID does not exist.'
    }

    void 'should return history when client exists'() {
        given:
            clientRepository.findById(_ as String) >> Optional.of(clientFromDatabase)
        when:
            Set<Loan> clientHistory = clientService.getClientHistory(CORRECT_USER_ID)
        then:
            clientFromDatabase.loans == clientHistory
    }

    void 'should fail loan postpone when loan not exist'() {
        given:
            loanRepository.findById(_ as Integer) >> Optional.empty()
        when:
            clientService.postponeLoan(1)
        then:
            NotFoundException e = thrown()
            e.message == 'Loan with id 1 does not exist.'
    }

    void 'should postpone loan when it is first postpone'() {
        given:
            loanRepository.findById(_ as Long) >> Optional.of(successfulLoan)
            loanRepository.save(_ as Loan) >> loanWithPostpone
        when:
            LoanPostpone loanPostponeResponse = clientService.postponeLoan(1)
        then:
            firstPostpone == loanPostponeResponse
    }

    void 'should postpone loan when it is not first postpone'() {
        given:
            loanRepository.findById(_ as Long) >> Optional.of(loanWithPostpone)
            loanRepository.save(_ as Loan) >> buildLoanWithPostpones(buildLoan(100.00), firstPostpone, secondPostpone)
        when:
            LoanPostpone loanPostponeResponse = clientService.postponeLoan(1)
        then:
            secondPostpone == loanPostponeResponse
    }

    private static Loan buildLoan(BigDecimal loanAmount) {
        new Loan().with {
            id = 1
            amount = loanAmount
            termInMonths = 12
            interestRate = 10
            returnDate = date.plusYears(1)
            return it
        } as Loan
    }

    private static LoanPostpone buildLoanPostpone(BigDecimal newRate, ZonedDateTime newDate) {
        new LoanPostpone().with {
            id = 1
            newInterestRate = newRate
            newReturnDate = newDate
            return it
        } as LoanPostpone
    }

    private static Client buildClient(Loan loanObj) {
        return new Client().with {
            id = CORRECT_USER_ID
            firstName = 'Testas'
            lastName = 'Testaitis'
            personalCode = 12345678910
            loan = loanObj
            return it
        } as Client
    }

    private static ZonedDateTime generateDate() {
        return ZonedDateTime.of(
            2020,
            1,
            1,
            1,
            1,
            1,
            1,
            ZoneId.of(TIME_ZONE))
    }

    private static Loan buildLoanWithPostpone(Loan loanRequest, LoanPostpone postpone1) {
        List<LoanPostpone> list = Collections.singletonList(postpone1)
        loanRequest.with { loanPostpones = new HashSet<>(list) }
        return loanRequest
    }

    private static Loan buildLoanWithPostpones(Loan loanRequest, LoanPostpone postpone1, LoanPostpone postpone2) {
        List<LoanPostpone> list = Arrays.asList(postpone1, postpone2)
        loanRequest.with { loanPostpones = new HashSet<>(list) }
        return loanRequest

    }

    private static ClientDAO buildClientResponse(Loan loanObj) {
        return new ClientDAO().with {
            id = CORRECT_USER_ID
            firstName = 'Testas'
            lastName = 'Testaitis'
            personalCode = 12345678910
            loans = new HashSet<>(Collections.singletonList(loanObj))
            return it
        } as ClientDAO
    }
}