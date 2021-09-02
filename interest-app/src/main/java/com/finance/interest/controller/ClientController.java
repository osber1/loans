package com.finance.interest.controller;

import java.util.Collection;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.interest.ClientRequest;
import com.finance.interest.ClientResponse;
import com.finance.interest.LoanPostponeResponse;
import com.finance.interest.LoanResponse;
import com.finance.interest.mapper.ClientMapper;
import com.finance.interest.mapper.LoanMapper;
import com.finance.interest.mapper.LoanPostponeMapper;
import com.finance.interest.model.Client;
import com.finance.interest.service.ClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    private final ClientMapper clientMapper;

    private final LoanMapper loanMapper;

    private final LoanPostponeMapper postponeMapper;

    @PostMapping("client/loans")
    public ClientResponse takeLoan(@Valid @RequestBody ClientRequest clientDto) {
        Client client = clientMapper.clientToEntity(clientDto);
        return clientMapper.clientToDTO(service.takeLoan(client));
    }

    @PostMapping("client/loans/{id}/extensions")
    public LoanPostponeResponse postponeLoan(@PathVariable long id) {
        return postponeMapper.loanPostponeToDTO(service.postponeLoan(id));
    }

    @GetMapping("client/{id}/loans")
    public Collection<LoanResponse> getClientHistory(@PathVariable String id) {
        return loanMapper.loanToDTOs(service.getClientHistory(id));
    }
}
