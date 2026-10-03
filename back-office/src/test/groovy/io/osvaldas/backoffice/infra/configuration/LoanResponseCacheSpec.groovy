package io.osvaldas.backoffice.infra.configuration

import static com.github.tomakehurst.wiremock.client.WireMock.okJson
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.loans.Status.NOT_EVALUATED
import static io.osvaldas.api.loans.Status.OPEN
import static io.osvaldas.api.loans.Status.REJECTED
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import java.sql.Timestamp
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.Cache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.context.ContextConfiguration
import org.springframework.transaction.support.TransactionTemplate
import org.wiremock.spring.ConfigureWireMock
import org.wiremock.spring.EnableWireMock
import org.wiremock.spring.InjectWireMock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock

import io.osvaldas.api.exceptions.ValidationRuleException
import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.api.loans.Status
import io.osvaldas.api.postpones.LoanPostponeResponse
import io.osvaldas.api.risk.validation.RiskRejectionReason
import io.osvaldas.api.risk.validation.RiskValidationResponse
import io.osvaldas.backoffice.domain.loans.LoanService
import io.osvaldas.backoffice.domain.scheduler.LoansTasksScheduler
import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.Loan

@EnableWireMock([@ConfigureWireMock(baseUrlProperties = 'risk.checker.url')])
@ContextConfiguration(classes = TestClockConfig)
@SpringBootTest(properties = 'spring.main.allow-bean-definition-overriding=true')
class LoanResponseCacheSpec extends AbstractControllerSpec {

    static final String CACHE_NAME = 'LoanResponse'

    static final String OTHER_CLIENT_ID = 'otherClientId'

    static final String OTHER_CLIENT_PERSONAL_CODE = '10987654321'

    static final RiskValidationResponse VALIDATION_PASSED = RiskValidationResponse.passed()

    static final RiskValidationResponse VALIDATION_FAILED =
        RiskValidationResponse.rejected(RiskRejectionReason.AMOUNT_EXCEEDS, AMOUNT_EXCEEDS)

    static final String UNTYPED_CACHE_NAME = 'UntypedCache'

    static final long CACHED_LOAN_ID = 42

    static final ZoneId VILNIUS = ZoneId.of('Europe/Vilnius')

    static final ZonedDateTime VILNIUS_DATE = ZonedDateTime.of(2021, 10, 12, 13, 10, 10, 123_456_789, VILNIUS)

    @InjectWireMock
    WireMockServer wireMock

    @Autowired
    StringRedisTemplate redisTemplate

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    TransactionTemplate transactionTemplate

    @Autowired
    LoanService loanService

    LoanResponse loanResponse = new LoanResponse(
        CACHED_LOAN_ID,
        100.00,
        10.50,
        LOAN_TERM_IN_MONTHS,
        OPEN,
        VILNIUS_DATE.plusMonths(LOAN_TERM_IN_MONTHS),
        DATE,
        [
            new LoanPostponeResponse(1, VILNIUS_DATE.plusWeeks(1), 15.75),
            new LoanPostponeResponse(2, DATE.plusWeeks(2), 23.63),
        ] as Set)

    void 'should store loan response in redis and read it back unchanged'() {
        given:
            Cache cache = cacheManager.getCache(CACHE_NAME)
        when:
            cache.put(CACHED_LOAN_ID, loanResponse)
        then:
            redisTemplate.hasKey("${CACHE_NAME}::${CACHED_LOAN_ID}".toString())
            redisTemplate.getExpire("${CACHE_NAME}::${CACHED_LOAN_ID}".toString()) in (3500L..3600L)
            !redisTemplate.opsForValue().get("${CACHE_NAME}::${CACHED_LOAN_ID}".toString()).contains('@class')
        and:
            LoanResponse cached = cache.get(CACHED_LOAN_ID, LoanResponse)
            cached == loanResponse
            cached.amount().scale() == 2
            cached.returnDate().zone == VILNIUS
            cached.loanPostpones()*.interestRate() as Set == [15.75, 23.63] as Set
        and:
            cache.get(CACHED_LOAN_ID).get() == loanResponse
    }

    void 'should read back loan response from cache without typed configuration'() {
        given:
            Cache cache = cacheManager.getCache(UNTYPED_CACHE_NAME)
        when:
            cache.put(CACHED_LOAN_ID, loanResponse)
        then:
            cache.get(CACHED_LOAN_ID).get() == loanResponse
            cache.get(CACHED_LOAN_ID, LoanResponse) == loanResponse
        cleanup:
            cache.clear()
    }

    void 'should serve loan from redis cache on repeated request'() {
        given:
            Loan loan = buildLoanWithoutId(100.00)
            [firstPostpone, secondPostpone].each { postpone ->
                postpone.id = 0
                postpone.loan = loan
                loan.addLoanPostpone(postpone)
            }
            Loan savedLoan = loanRepository.save(loan)
        when:
            MockHttpServletResponse firstResponse = getLoan(savedLoan.id)
        then:
            firstResponse.status == OK.value()
            redisTemplate.hasKey("${CACHE_NAME}::${savedLoan.id}".toString())
        when:
            loanRepository.deleteAll()
        and:
            MockHttpServletResponse cachedResponse = getLoan(savedLoan.id)
        then:
            cachedResponse.status == OK.value()
            objectMapper.readValue(cachedResponse.contentAsString, LoanResponse) ==
                objectMapper.readValue(firstResponse.contentAsString, LoanResponse)
        and:
            with(objectMapper.readValue(cachedResponse.contentAsString, LoanResponse)) {
                id() == savedLoan.id
                amount() == 100.00
                loanPostpones().size() == 2
                loanPostpones()*.interestRate() as Set == [15.00, 22.50] as Set
            }
        cleanup:
            jdbcTemplate.execute('ALTER SEQUENCE postpone_seq RESTART WITH 1')
    }

    void 'should show new postpone after cached loan is postponed'() {
        given:
            Loan savedLoan = saveClientLoan(OPEN)
        when:
            LoanResponse cachedLoan = requestLoan(savedLoan.id)
        then:
            cachedLoan.loanPostpones().empty
        when:
            MockHttpServletResponse postponeResponse = mockMvc.perform(post('/api/v1/loans/extensions')
                .param('loanId', "${savedLoan.id}"))
                .andReturn().response
        then:
            postponeResponse.status == OK.value()
            with(requestLoan(savedLoan.id).loanPostpones()) {
                it*.interestRate() == [firstPostpone.interestRate]
                it*.returnDate()*.toInstant() == [firstPostpone.returnDate.toInstant()]
            }
    }

    void 'should show opened loan after cached loan passes validation'() {
        given:
            Loan savedLoan = saveClientLoan(NOT_EVALUATED)
            stubRiskValidation(VALIDATION_PASSED)
        when:
            LoanResponse cachedLoan = requestLoan(savedLoan.id)
        then:
            cachedLoan.status() == NOT_EVALUATED
        when:
            loanService.validate(savedLoan, CLIENT_ID)
        then:
            requestLoan(savedLoan.id).status() == OPEN
    }

    void 'should show rejected loan after cached loan fails validation'() {
        given:
            Loan savedLoan = saveClientLoan(NOT_EVALUATED)
            stubRiskValidation(VALIDATION_FAILED)
        when:
            LoanResponse cachedLoan = requestLoan(savedLoan.id)
        then:
            cachedLoan.status() == NOT_EVALUATED
        when:
            loanService.validate(savedLoan, CLIENT_ID)
        then:
            thrown(ValidationRuleException)
            requestLoan(savedLoan.id).status() == REJECTED
    }

    void 'should show #status loan after cached loan is evaluated by scheduler'() {
        given:
            Loan savedLoan = saveClientLoan(NOT_EVALUATED)
            transactionTemplate.executeWithoutResult {
                jdbcTemplate.update('UPDATE loan SET created_at = ? WHERE id = ?',
                    Timestamp.from(Instant.parse('2021-10-12T10:00:00Z')), savedLoan.id)
            }
            stubRiskValidation(validationResponse)
        when:
            LoanResponse cachedLoan = requestLoan(savedLoan.id)
        then:
            cachedLoan.status() == NOT_EVALUATED
        when:
            new LoansTasksScheduler(loanService, Duration.ofMinutes(5)).evaluateNotEvaluatedLoans()
        then:
            requestLoan(savedLoan.id).status() == status
        where:
            validationResponse || status
            VALIDATION_PASSED  || OPEN
            VALIDATION_FAILED  || REJECTED
    }

    void 'should keep other client loan cached when a new loan is taken'() {
        given:
            Loan otherClientLoan = clientRepository.save(
                buildClient(OTHER_CLIENT_ID, [buildLoanWithoutId(100.00)] as Set, ACTIVE)
                    .tap { personalCode = OTHER_CLIENT_PERSONAL_CODE })
                .loans.first()
            clientRepository.save(activeClientWithId)
            stubRiskValidation(VALIDATION_PASSED)
        and:
            LoanResponse cachedLoan = requestLoan(otherClientLoan.id)
            loanRepository.save(otherClientLoan.tap { amount = 200.00 })
        when:
            MockHttpServletResponse takeLoanResponse = mockMvc.perform(post('/api/v1/loans')
                .param('clientId', CLIENT_ID)
                .content(objectMapper.writeValueAsString(buildLoanRequest(100.00)))
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            takeLoanResponse.status == OK.value()
        and:
            with(requestLoan(otherClientLoan.id)) {
                it == cachedLoan
                amount() == 100.00
            }
    }

    private Loan saveClientLoan(Status loanStatus) {
        clientRepository.save(buildClient(CLIENT_ID, [buildLoanWithoutId(100.00, loanStatus)] as Set, ACTIVE))
            .loans.first()
    }

    private void stubRiskValidation(RiskValidationResponse response) {
        wireMock.stubFor(WireMock.post(urlPathEqualTo('/api/v1/validation'))
            .willReturn(okJson(objectMapper.writeValueAsString(response))))
    }

    private LoanResponse requestLoan(long loanId) {
        objectMapper.readValue(getLoan(loanId).contentAsString, LoanResponse)
    }

    private MockHttpServletResponse getLoan(long loanId) {
        mockMvc.perform(get('/api/v1/loans/{loanId}', loanId)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

}
