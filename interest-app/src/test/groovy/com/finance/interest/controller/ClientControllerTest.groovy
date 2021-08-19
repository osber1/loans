package com.finance.interest.controller

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper
import com.finance.interest.ClientRequest
import com.finance.interest.ClientResponse
import com.finance.interest.LoanRequest
import com.finance.interest.LoanResponse
import com.finance.interest.model.ClientDAO
import com.finance.interest.model.Loan
import com.finance.interest.model.LoanPostpone
import com.finance.interest.repository.ClientRepository
import com.finance.interest.repository.LoanRepository

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import spock.lang.Specification

@SpringBootTest
@AutoConfigureMockMvc
class ClientControllerTest extends Specification {

    private static final ZonedDateTime date = generateDate()

    private static final String TIME_ZONE = "Europe/Vilnius"

    private static final String NAME = "Name"

    private static final String SURNAME = "Surname"

    private static final String PERSONAL_CODE = "12345678910"

    private static final String ID = "CORRECT-TEST-ID"

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

    private ClientDAO clientDao = buildClientDao()

    private LoanPostpone loanPostpone = buildLoanPostpone()

    void cleanup() {
        clientRepository.deleteAll()
        loanRepository.deleteAll()
        for (String key : redisTemplate.keys("*")) {
            redisTemplate.delete(key);
        }
    }

    void 'should take loan when request is correct'() {
        given:
            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, PERSONAL_CODE, buildLoanRequest())
        when:
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans")
                .requestAttr("remoteAddr", "0.0.0.0.0.1")
                .content(new JsonBuilder(clientRequest) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            ClientResponse clientResponse = objectMapper.readValue(response.contentAsString, ClientResponse.class)
            with(clientResponse) {
                firstName == clientRequest.firstName
                lastName == clientRequest.lastName
                personalCode == clientRequest.personalCode
                loan.amount == clientRequest.loan.amount
                loan.termInMonths == clientRequest.loan.termInMonths
            }
    }

    void 'should throw exception when client\'s first name is null'() {
        given:
            ClientRequest clientRequest = buildClientRequest(null, SURNAME, PERSONAL_CODE, buildLoanRequest())
        when:
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans")
                .requestAttr("remoteAddr", "0.0.0.0.0.1")
                .content(new JsonBuilder(clientRequest) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            response.contentAsString.contains('First name must be not empty.')
    }

    void 'should throw exception when client\'s last name is null'() {
        given:
            ClientRequest clientRequest = buildClientRequest(NAME, null, PERSONAL_CODE, buildLoanRequest())
        when:
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans")
                .requestAttr("remoteAddr", "0.0.0.0.0.1")
                .content(new JsonBuilder(clientRequest) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            response.contentAsString.contains('Last name must be not empty.')
    }

    void 'should throw exception when client\'s personal code is null'() {
        given:
            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, null, buildLoanRequest())
        when:
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans")
                .requestAttr("remoteAddr", "0.0.0.0.0.1")
                .content(new JsonBuilder(clientRequest) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            response.contentAsString.contains('Personal code must be not empty.')
    }

    void 'should throw exception when client\'s personal code is too short'() {
        given:
            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, '123456789', buildLoanRequest())
        when:
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans")
                .requestAttr("remoteAddr", "0.0.0.0.0.1")
                .content(new JsonBuilder(clientRequest) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            response.contentAsString.contains('Personal code must be 11 digits length.')
    }

    void 'should throw exception when client\'s personal code contains letters'() {
        given:
            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, '123456789aa', buildLoanRequest())
        when:
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans")
                .requestAttr("remoteAddr", "0.0.0.0.0.1")
                .content(new JsonBuilder(clientRequest) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            response.contentAsString.contains('All characters must be digits.')
    }

    void 'should throw exception when client\'s loan is null'() {
        given:
            ClientRequest clientRequest = buildClientRequest(NAME, SURNAME, PERSONAL_CODE, null)
        when:
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans")
                .requestAttr("remoteAddr", "0.0.0.0.0.1")
                .content(new JsonBuilder(clientRequest) as String)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.BAD_REQUEST.value()
        and:
            response.contentAsString.contains('Loan must be not empty.')
    }

    void 'should return client history when it exists'() {
        given:
            clientRepository.save(clientDao)
        when:
            MockHttpServletResponse response = mockMvc.perform(get("/api/client/{id}/loans", ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.OK.value()
        and:
            List<Loan> responseLoanList = new JsonSlurper().parseText(response.contentAsString) as List<Loan>
            Loan actualLoan = clientDao.loans[0]
            with(responseLoanList.first()) {
                amount == actualLoan.amount
                interestRate == actualLoan.interestRate
                termInMonths == actualLoan.termInMonths
                returnDate.toString() == actualLoan.returnDate.toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS).toString()
            }
    }

    void 'should throw an exception when trying to get client history and it not exist'() {
        when:
            MockHttpServletResponse response = mockMvc.perform(get("/api/client/{id}/loans", 5555)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            response.contentAsString.contains('Client with id 5555 does not exist.')
    }

    void 'should postpone loan when it exists'() {
        given:
            ClientDAO savedClient = clientRepository.save(clientDao)
        when:
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans/{id}/extensions", savedClient.getLoans()[0].getId())
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
            MockHttpServletResponse response = mockMvc.perform(post("/api/client/loans/{id}/extensions", 5555)
                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == HttpStatus.NOT_FOUND.value()
        and:
            response.contentAsString.contains('Loan with id 5555 does not exist.')
    }

    private static ClientRequest buildClientRequest(
        String clientName,
        String clientSurname,
        String clientCode,
        LoanRequest clientLoan) {
        return new ClientRequest().with {
            firstName = clientName
            lastName = clientSurname
            personalCode = clientCode
            loan = clientLoan
            return it
        }
    }

    private static LoanRequest buildLoanRequest() {
        return new LoanRequest().with {
            amount = 100.00
            termInMonths = 12
            return it
        }
    }

    private static Loan buildLoan() {
        return new Loan().with {
            id = 1
            amount = 100.00
            interestRate = 10.00
            termInMonths = 12
            returnDate = date.plusYears(1)
            loanPostpones = []
            return it
        } as Loan
    }

    private static ClientResponse buildClientResponse() {
        LoanResponse loanResponse = new LoanResponse().with {
            amount = 100.00
            interestRate = 10.00
            termInMonths = 12
            returnDate = date.plusYears(1)
            return it
        }

        return new ClientResponse().with {
            firstName = NAME
            lastName = SURNAME
            personalCode = PERSONAL_CODE
            loan = loanResponse
            return it
        } as ClientResponse
    }

    private static ClientDAO buildClientDao() {
        return new ClientDAO().with {
            id = ID
            firstName = NAME
            lastName = SURNAME
            personalCode = Long.parseLong(PERSONAL_CODE)
            loans = Set.of(buildLoan())
            return it
        } as ClientDAO
    }

    private static LoanPostpone buildLoanPostpone() {
        return new LoanPostpone().with {
            id = 1
            newReturnDate = date.plusWeeks(1)
            newInterestRate = 15.00
            return it
        } as LoanPostpone
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
}