package io.osvaldas.backoffice.infra.rest.revisions;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.backoffice.domain.revisions.RevisionService;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/revisions")
public class RevisionsController {

    private final RevisionService service;

    @GetMapping("clients/{clientId}")
    public List<ClientResponse> getClientRevisions(@PathVariable String clientId) {
        return service.getClientRevisions(clientId);
    }

    @GetMapping("loans/{loanId}")
    public List<LoanResponse> getLoansRevisions(@PathVariable long loanId) {
        return service.getLoanRevisions(loanId);
    }

}
