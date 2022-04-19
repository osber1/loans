package io.osvaldas.backoffice.infra.rest.loans;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.api.loans.LoanRequest;
import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.api.loans.TodayTakenLoansCount;
import io.osvaldas.backoffice.domain.loans.LoanService;
import io.osvaldas.backoffice.repositories.entities.Loan;
import io.osvaldas.backoffice.repositories.mapper.LoanMapper;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class LoansController {

    private final LoanService service;

    private final LoanMapper loanMapper;

    @GetMapping("loans/{loanId}")
    public LoanResponse getLoan(@PathVariable long loanId) {
        return loanMapper.loanToDTO(service.getLoan(loanId));
    }

    @GetMapping("client/{clientId}/loans")
    public Collection<LoanResponse> getClientHistory(@PathVariable String clientId) {
        return loanMapper.loanToDTOs(service.getLoans(clientId));
    }

    @GetMapping("client/{clientId}/loans/today")
    public TodayTakenLoansCount getTodayTakenLoansCount(@PathVariable String clientId) {
        return service.getTodayTakenLoansCount(clientId);
    }

    @PostMapping("client/{clientId}/loan")
    public LoanResponse takeLoan(@PathVariable String clientId, @Valid @RequestBody LoanRequest request) {
        Loan loan = loanMapper.loanToEntity(request);
        Loan takenLoan = service.addLoan(loan, clientId);
        service.validate(takenLoan, clientId);
        return loanMapper.loanToDTO(takenLoan);
    }
}
