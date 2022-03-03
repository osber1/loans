package com.finance.loans.infra.rest;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.loans.domain.clients.ClientService;
import com.finance.loans.infra.rest.dtos.ClientRequest;
import com.finance.loans.infra.rest.dtos.ClientResponse;
import com.finance.loans.infra.rest.dtos.LoanPostponeResponse;
import com.finance.loans.infra.rest.dtos.LoanRequest;
import com.finance.loans.infra.rest.dtos.LoanResponse;
import com.finance.loans.repositories.entities.Client;
import com.finance.loans.repositories.entities.Loan;
import com.finance.loans.repositories.mapper.ClientMapper;
import com.finance.loans.repositories.mapper.LoanMapper;
import com.finance.loans.repositories.mapper.LoanPostponeMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    private final ClientMapper clientMapper;

    private final LoanMapper loanMapper;

    private final LoanPostponeMapper postponeMapper;

    @PostMapping("client")
    public ClientResponse registerClient(@Valid @RequestBody ClientRequest request) {
        Client client = clientMapper.clientToEntity(request);
        return clientMapper.clientToDTO(service.registerClient(client));
    }

    @GetMapping("clients")
    public Collection<ClientResponse> getClients() {
        return clientMapper.clientsToDTOs(service.getClients());
    }

    @GetMapping("client/{id}")
    public ClientResponse getClient(@PathVariable String id) {
        return clientMapper.clientToDTO(service.getClient(id));
    }

    @PutMapping("client")
    public ClientResponse updateClient(@Valid @RequestBody ClientRequest request) {
        Client client = clientMapper.clientToEntity(request);
        return clientMapper.clientToDTO(service.updateClient(client));
    }

    @DeleteMapping("client/{id}")
    public void deleteClient(@PathVariable String id) {
        service.deleteClient(id);
    }

    @GetMapping("client/{id}/loans")
    public Collection<LoanResponse> getClientHistory(@PathVariable String id) {
        return loanMapper.loanToDTOs(service.getClient(id).getLoans());
    }

    @PostMapping("client/{id}/loan")
    public LoanResponse takeLoan(@PathVariable String id, @Valid @RequestBody LoanRequest request) {
        Loan loan = loanMapper.loanToEntity(request);
        return loanMapper.loanToDTO(service.takeLoan(loan, id));
    }

    @PostMapping("client/loans/{id}/extensions")
    public LoanPostponeResponse postponeLoan(@PathVariable long id) {
        return postponeMapper.loanPostponeToDTO(service.postponeLoan(id));
    }
}
