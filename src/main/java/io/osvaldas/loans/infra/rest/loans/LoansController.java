package io.osvaldas.loans.infra.rest.loans;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.loans.domain.loans.LoanService;
import io.osvaldas.loans.infra.rest.loans.dtos.LoanRequest;
import io.osvaldas.loans.infra.rest.loans.dtos.LoanResponse;
import io.osvaldas.loans.repositories.entities.Loan;
import io.osvaldas.loans.repositories.mapper.LoanMapper;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class LoansController {

    private final LoanService service;

    private final LoanMapper loanMapper;

    @GetMapping("client/loans/{loanId}")
    public LoanResponse getLoans(@PathVariable long loanId) {
        return loanMapper.loanToDTO(service.getLoan(loanId));
    }

    @GetMapping("client/{clientId}/loans")
    public Collection<LoanResponse> getClientHistory(@PathVariable String clientId) {
        return loanMapper.loanToDTOs(service.getLoans(clientId));
    }

    @PostMapping("client/{clientId}/loan")
    public LoanResponse takeLoan(@PathVariable String clientId, @Valid @RequestBody LoanRequest request) {
        Loan loan = loanMapper.loanToEntity(request);
        return loanMapper.loanToDTO(service.takeLoan(loan, clientId));
    }
}
