package io.osvaldas.backoffice.infra.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.web.servlet.MockMvc

import com.fasterxml.jackson.databind.ObjectMapper
import com.jupitertools.springtestredis.RedisTestContainer

import io.osvaldas.backoffice.AbstractSpec
import io.osvaldas.backoffice.domain.loans.validators.IpValidator
import io.osvaldas.backoffice.infra.rest.clients.dtos.ClientRegisterRequest
import io.osvaldas.backoffice.infra.rest.loans.dtos.LoanRequest
import io.osvaldas.backoffice.repositories.ClientRepository
import io.osvaldas.backoffice.repositories.LoanRepository

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

    @Value('${exceptionMessages.ipExceedsMessage:}')
    String ipExceedsMessage

    @Value('${exceptionMessages.clientNotActiveMessage:}')
    String clientNotActiveMessage

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
        redisTemplate.keys('*').each { redisTemplate.delete(it) }
    }

    ClientRegisterRequest buildClientRequest(
        String clientName,
        String clientSurname,
        String clientCode,
        String clientEmail,
        String clientPhoneNumber) {
        new ClientRegisterRequest().tap {
            firstName = clientName
            lastName = clientSurname
            personalCode = clientCode
            email = clientEmail
            phoneNumber = clientPhoneNumber
        }
    }

    LoanRequest buildLoanRequest(BigDecimal loanAmount) {
        new LoanRequest().tap {
            amount = loanAmount
            termInMonths = 12
        }
    }

}