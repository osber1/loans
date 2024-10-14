package io.osvaldas.backoffice.infra.rest.revisions;

import java.util.List;

import org.springframework.data.history.Revision;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.backoffice.repositories.ClientRepository;
import io.osvaldas.backoffice.repositories.LoanRepository;
import io.osvaldas.backoffice.repositories.mapper.ClientMapper;
import io.osvaldas.backoffice.repositories.mapper.LoanMapper;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/revisions")
public class RevisionsController {

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    private final LoanRepository loanRepository;

    private final LoanMapper loanMapper;

    @GetMapping("clients/{clientId}")
    public List<ClientResponse> getClientRevisions(@PathVariable String clientId) {
        return clientRepository.findRevisions(clientId).stream()
            .map(Revision::getEntity)
            .map(clientMapper::map)
            .toList();
    }

    @GetMapping("loans/{loanId}")
    public List<LoanResponse> getLoansRevisions(@PathVariable long loanId) {
        return loanRepository.findRevisions(loanId).stream()
            .map(Revision::getEntity)
            .map(loanMapper::map)
            .toList();
    }

}
