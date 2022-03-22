package com.finance.loans.infra.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.finance.loans.domain.loans.validators.IpValidator
import com.finance.loans.domain.loans.validators.IpValidator.IpException
import com.finance.loans.infra.rest.dtos.ClientRequest
import com.finance.loans.infra.rest.dtos.LoanRequest
import com.finance.loans.infra.rest.dtos.LoanResponse
import com.finance.loans.repositories.ClientRepository
import com.finance.loans.repositories.LoanRepository
import com.finance.loans.repositories.entities.Client
import com.finance.loans.repositories.entities.Loan
import com.finance.loans.repositories.entities.LoanPostpone
import com.jupitertools.springtestredis.RedisTestContainer
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Shared
import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

@SpringBootTest
@RedisTestContainer
@AutoConfigureMockMvc
class ClientControllerSpec extends Specification {

    @Shared
    String TIME_ZONE = 'Europe/Vilnius'

    @Shared
    String NAME = 'Name'

    @Shared
    String SURNAME = 'Surname'

    @Shared
    String PERSONAL_CODE = '12345678910'

    @Shared
    String EMAIL = 'test@mail.com'

    @Shared
    String PHONE_NUMBER = '+37062514361'

    @Shared
    String ID = 'CORRECT-TEST-ID'
    @Shared
    ZonedDateTime date = generateDate()
    @Autowired
    private MockMvc mockMvc

    @Autowired
    private ObjectMapper objectMapper

    @Autowired
    private ClientRepository clientRepository

    @Autowired
    private LoanRepository loanRepository

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate

    @Autowired
    private IpValidator ipValidator

    private Client client = buildClient()

    private LoanPostpone loanPostpone = buildLoanPostpone()

    void cleanup() {
        clientRepository.deleteAll()
        loanRepository.deleteAll()
        for (String key : redisTemplate.keys('*')) {
            redisTemplate.delete(key)
        }
    }

    void 'should fail when ip limit is exceeded taking loans'() {
        given:
        LoanRequest loanRequest = new LoanRequest().tap {
            amount = 50
            termInMonths = 5
        }
        when:
        postLoanRequest(loanRequest, ID)
        postLoanRequest(loanRequest, ID)
        then:
        IpException exception = thrown()
        exception.message == 'Too many requests from the same ip per day.'
    }

    void 'should take loan when request is correct'() {
        given:
        clientRepository.save(client)
        LoanRequest request = new LoanRequest().tap {
            amount = 50
            termInMonths = 5
        }
        when:
        MockHttpServletResponse response = postLoanRequest(request, ID)
        then:
        response.status == HttpStatus.OK.value()
        and:
        LoanResponse loanResponse = objectMapper.readValue(response.contentAsString, LoanResponse)
        with(loanResponse) {
            amount == request.amount
            termInMonths == request.termInMonths
        }
    }

    void 'should throw exception when client\'s first name is null'() {
        given:
        ClientRequest clientRequest = buildClientRequest(null, SURNAME, PERSONAL_CODE, EMAIL, PHONE_NUMBER)
        when:
        MockHttpServletResponse response = postClientRequest(clientRequest)
        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        and:
        response.contentAsString.contains('First name must be not empty.')
    }

    void 'should throw exception when client\'s last name is null'() {
        given:
        ClientRequest clientRequest = buildClientRequest(NAME, null, PERSONAL_CODE, EMAIL, PHONE_NUMBER)
        when:
        MockHttpServletResponse response = postClientRequest(clientRequest)
        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        and:
        response.contentAsString.contains('Last name must be not empty.')
    }

    void 'should throw exception when client\'s personal code is null'() {
        given:
        ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, null, EMAIL, PHONE_NUMBER)
        when:
        MockHttpServletResponse response = postClientRequest(clientRequest)
        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        and:
        response.contentAsString.contains('Personal code must be not empty.')
    }

    void 'should throw exception when client\'s personal code is too short'() {
        given:
        ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, '123456789', EMAIL, PHONE_NUMBER)
        when:
        MockHttpServletResponse response = postClientRequest(clientRequest)
        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        and:
        response.contentAsString.contains('Personal code must be 11 digits length.')
    }

    void 'should throw exception when client\'s personal code contains letters'() {
        given:
        ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, '123456789aa', EMAIL, PHONE_NUMBER)
        when:
        MockHttpServletResponse response = postClientRequest(clientRequest)
        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        and:
        response.contentAsString.contains('All characters must be digits.')
    }

    void 'should throw exception when client\'s email is null'() {
        given:
        ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, PERSONAL_CODE, null, PHONE_NUMBER)
        when:
        MockHttpServletResponse response = postClientRequest(clientRequest)
        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        and:
        response.contentAsString.contains('Email must be not empty.')
    }

    void 'should throw exception when client\'s email is wrong'() {
        given:
        ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, PERSONAL_CODE, 'bad email', PHONE_NUMBER)
        when:
        MockHttpServletResponse response = postClientRequest(clientRequest)
        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        and:
        response.contentAsString.contains('must be a well-formed email address')
    }

    void 'should throw exception when client\'s phone number is null'() {
        given:
        ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, PERSONAL_CODE, EMAIL, null)
        when:
        MockHttpServletResponse response = postClientRequest(clientRequest)
        then:
        response.status == HttpStatus.BAD_REQUEST.value()
        and:
        response.contentAsString.contains('Phone number must be not empty.')
    }

    void 'should return client history when it exists'() {
        given:
        clientRepository.save(client)
        when:
        MockHttpServletResponse response = mockMvc.perform(get('/api/client/{id}/loans', ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
        response.status == HttpStatus.OK.value()
        and:
        List<Loan> responseLoanList = new JsonSlurper().parseText(response.contentAsString) as List<Loan>
        Loan actualLoan = client.loans[0]
        with(responseLoanList.first()) {
            amount == actualLoan.amount
            interestRate == actualLoan.interestRate
            termInMonths == actualLoan.termInMonths
            returnDate.toString() == actualLoan.returnDate.toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS).toString()
        }
    }

    void 'should throw an exception when trying to get client history and it not exist'() {
        when:
        MockHttpServletResponse response = mockMvc.perform(get('/api/client/{id}/loans', 5555)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
        response.status == HttpStatus.NOT_FOUND.value()
        and:
        response.contentAsString.contains('Client with id 5555 does not exist.')
    }

    void 'should postpone loan when it exists'() {
        given:
        Client savedClient = clientRepository.save(client)
        when:
        MockHttpServletResponse response = mockMvc.perform(post('/api/client/loans/{id}/extensions', savedClient.getLoans()[0].getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
        response.status == HttpStatus.OK.value()
        and:
        LoanPostpone postpone = objectMapper.readValue(response.contentAsString, LoanPostpone.class)
        with(postpone) {
            id == loanPostpone.id
            newInterestRate == loanPostpone.newInterestRate
        }
    }

    void 'should throw an exception when trying to postpone loan'() {
        when:
        MockHttpServletResponse response = mockMvc.perform(post('/api/client/loans/{id}/extensions', 5555)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
        response.status == HttpStatus.NOT_FOUND.value()
        and:
        response.contentAsString.contains('Loan with id 5555 does not exist.')
    }

    private MockHttpServletResponse postClientRequest(ClientRequest request) {
        mockMvc.perform(post('/api/client')
                .content(new JsonBuilder(request) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
    }

    private MockHttpServletResponse postLoanRequest(LoanRequest request, String id) {
        mockMvc.perform(post('/api/client/' + id + '/loan')
                .requestAttr('remoteAddr', '0.0.0.0.0.1')
                .content(new JsonBuilder(request) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
    }

    private ClientRequest buildClientRequest(
            String clientName,
            String clientSurname,
            String clientCode,
            String clientEmail,
            String clientPhoneNumber) {
        return new ClientRequest().tap {
            firstName = clientName
            lastName = clientSurname
            personalCode = clientCode
            email = clientEmail
            phoneNumber = clientPhoneNumber
        }
    }

    private LoanRequest buildLoanRequest() {
        return new LoanRequest().tap {
            amount = 100.00
            termInMonths = 12
        }
    }

    private Loan buildLoan() {
        return new Loan().tap {
            id = 1
            amount = 100.00
            interestRate = 10.00
            termInMonths = 12
            returnDate = date.plusYears(1)
            loanPostpones = []
        } as Loan
    }

    private Client buildClient() {
        return new Client().tap {
            id = ID
            firstName = NAME
            lastName = SURNAME
            email = EMAIL
            phoneNumber = PHONE_NUMBER
            personalCode = Long.parseLong(PERSONAL_CODE)
            loans = Set.of(buildLoan())
        } as Client
    }

    private LoanPostpone buildLoanPostpone() {
        return new LoanPostpone().tap {
            id = 1
            newReturnDate = date.plusWeeks(1)
            newInterestRate = 15.00
        } as LoanPostpone
    }

    private ZonedDateTime generateDate() {
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
}