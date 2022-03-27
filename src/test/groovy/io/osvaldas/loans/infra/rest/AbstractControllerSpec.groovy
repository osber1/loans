package io.osvaldas.loans.infra.rest

import static java.lang.Long.parseLong
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import java.time.ZoneId
import java.time.ZonedDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper
import com.jupitertools.springtestredis.RedisTestContainer

import groovy.json.JsonBuilder
import io.osvaldas.loans.domain.loans.validators.IpValidator
import io.osvaldas.loans.infra.rest.clients.dtos.ClientRequest
import io.osvaldas.loans.infra.rest.loans.dtos.LoanRequest
import io.osvaldas.loans.repositories.ClientRepository
import io.osvaldas.loans.repositories.LoanRepository
import io.osvaldas.loans.repositories.entities.Client
import io.osvaldas.loans.repositories.entities.Loan
import io.osvaldas.loans.repositories.entities.LoanPostpone
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest
@RedisTestContainer
@AutoConfigureMockMvc
abstract class AbstractControllerSpec extends Specification {

    @Shared
    String timeZone = 'Europe/Vilnius'

    @Shared
    String name = 'Name'

    @Shared
    String surname = 'Surname'

    @Shared
    String clientPersonalCode = '12345678910'

    @Shared
    String email = 'test@mail.com'

    @Shared
    String phoneNumber = '+37062514361'

    @Shared
    String clientId = 'clientId'

    @Shared
    ZonedDateTime date = generateDate()

    @Autowired
    MockMvc mockMvc

    @Autowired
    ObjectMapper objectMapper

    @Autowired
    ClientRepository clientRepository

    @Autowired
    LoanRepository loanRepository

    @Autowired
    RedisTemplate<String, Integer> redisTemplate

    @Autowired
    IpValidator ipValidator

    Client client = buildClient()

    LoanPostpone loanPostpone = buildLoanPostpone()

    void cleanup() {
        clientRepository.deleteAll()
        loanRepository.deleteAll()
        for (String key : redisTemplate.keys('*')) {
            redisTemplate.delete(key)
        }
    }

    MockHttpServletResponse postClientRequest(ClientRequest request) {
        mockMvc.perform(post('/api/client')
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

    MockHttpServletResponse postLoanRequest(LoanRequest request, String id) {
        mockMvc.perform(post('/api/client/' + id + '/loan')
            .requestAttr('remoteAddr', '0.0.0.0.0.1')
            .content(new JsonBuilder(request) as String)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

    ClientRequest buildClientRequest(
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

    LoanRequest buildLoanRequest() {
        return new LoanRequest().tap {
            amount = 100.00
            termInMonths = 12
        }
    }

    Loan buildLoan() {
        return new Loan().tap {
            id = 1
            amount = 100.00
            interestRate = 10.00
            termInMonths = 12
            returnDate = date.plusYears(1)
            loanPostpones = []
        }
    }

    Client buildClient() {
        return new Client().tap {
            id = clientId
            firstName = name
            lastName = surname
            email = email
            phoneNumber = phoneNumber
            personalCode = parseLong(clientPersonalCode)
            loans = Set.of(buildLoan())
        }
    }

    LoanPostpone buildLoanPostpone() {
        return new LoanPostpone().tap {
            id = 1
            newReturnDate = date.plusWeeks(1)
            newInterestRate = 15.00
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