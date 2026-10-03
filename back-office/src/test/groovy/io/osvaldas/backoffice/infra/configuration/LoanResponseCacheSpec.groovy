package io.osvaldas.backoffice.infra.configuration

import static io.osvaldas.api.loans.Status.OPEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import java.time.ZoneId
import java.time.ZonedDateTime

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.Cache
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpServletResponse

import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.api.postpones.LoanPostponeResponse
import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.Loan

class LoanResponseCacheSpec extends AbstractControllerSpec {

    static final String CACHE_NAME = 'LoanResponse'

    static final String UNTYPED_CACHE_NAME = 'UntypedCache'

    static final long CACHED_LOAN_ID = 42

    static final ZoneId VILNIUS = ZoneId.of('Europe/Vilnius')

    static final ZonedDateTime VILNIUS_DATE = ZonedDateTime.of(2021, 10, 12, 13, 10, 10, 123_456_789, VILNIUS)

    @Autowired
    StringRedisTemplate redisTemplate

    @Autowired
    JdbcTemplate jdbcTemplate

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

    private MockHttpServletResponse getLoan(long loanId) {
        mockMvc.perform(get('/api/v1/loans/{loanId}', loanId)
            .contentType(APPLICATION_JSON))
            .andReturn().response
    }

}
