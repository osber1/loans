package io.osvaldas.loans.infra.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper
import com.jupitertools.springtestredis.RedisTestContainer

import io.osvaldas.loans.AbstractSpec
import io.osvaldas.loans.domain.loans.validators.IpValidator
import io.osvaldas.loans.infra.rest.clients.dtos.ClientRequest
import io.osvaldas.loans.infra.rest.loans.dtos.LoanRequest
import io.osvaldas.loans.repositories.ClientRepository
import io.osvaldas.loans.repositories.LoanRepository

@SpringBootTest
@RedisTestContainer
@AutoConfigureMockMvc
abstract class AbstractControllerSpec extends AbstractSpec {

    @Value('${exceptionMessages.clientErrorMessage:}')
    String clientErrorMessage

    @Value('${exceptionMessages.loanErrorMessage:}')
    String loanErrorMessage

    @Value('${exceptionMessages.riskMessage:}')
    String riskMessage

    @Value('${exceptionMessages.amountExceedsMessage:}')
    String amountExceedsMessage

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

    void cleanup() {
        clientRepository.deleteAll()
        loanRepository.deleteAll()
        for (String key : redisTemplate.keys('*')) {
            redisTemplate.delete(key)
        }
    }

    ClientRequest buildClientRequest(
        String clientName,
        String clientSurname,
        String clientCode,
        String clientEmail,
        String clientPhoneNumber) {
        new ClientRequest().tap {
            firstName = clientName
            lastName = clientSurname
            personalCode = clientCode
            email = clientEmail
            phoneNumber = clientPhoneNumber
        }
    }

    LoanRequest buildLoanRequest() {
        new LoanRequest().tap {
            amount = 100.00
            termInMonths = 12
        }
    }
}