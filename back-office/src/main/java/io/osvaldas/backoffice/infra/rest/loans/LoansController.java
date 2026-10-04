package io.osvaldas.backoffice.infra.rest.loans;

import static io.osvaldas.backoffice.infra.configuration.BeansConfig.LOAN_RESPONSE_CACHE;

import java.util.Collection;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;
import io.osvaldas.api.loans.LoanRequest;
import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.backoffice.domain.loans.LoanService;
import io.osvaldas.backoffice.domain.loans.RiskCheckerErrors;
import io.osvaldas.backoffice.repositories.entities.Loan;
import io.osvaldas.backoffice.repositories.mapper.LoanMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class LoansController {

    private final LoanService service;

    private final LoanMapper loanMapper;

    @Cacheable(cacheNames = LOAN_RESPONSE_CACHE, key = "#loanId")
    @GetMapping("loans/{loanId}")
    public LoanResponse getLoan(@PathVariable long loanId) {
        return loanMapper.map(service.getLoan(loanId));
    }

    @GetMapping("loans")
    public Collection<LoanResponse> getClientHistory(@RequestParam String clientId) {
        return loanMapper.map(service.getLoans(clientId));
    }

    @PostMapping("loans")
    public ResponseEntity<LoanResponse> takeLoan(@RequestParam String clientId, @Valid @RequestBody LoanRequest request) {
        Loan loan = loanMapper.map(request);
        Loan takenLoan = service.addLoan(loan, clientId);
        try {
            service.validate(takenLoan, clientId);
        } catch (FeignException e) {
            if (!RiskCheckerErrors.isUnavailable(e)) {
                throw e;
            }
            log.warn("Risk checker is unavailable, loan {} will be evaluated later", takenLoan.getId(), e);
            return ResponseEntity.accepted().body(loanMapper.map(takenLoan));
        }
        return ResponseEntity.ok(loanMapper.map(takenLoan));
    }
}
