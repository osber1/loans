package io.osvaldas.backoffice.infra.rest.revisions;

import java.util.List;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.backoffice.repositories.entities.Loan;
import io.osvaldas.backoffice.repositories.mapper.ClientMapper;
import io.osvaldas.backoffice.repositories.mapper.LoanMapper;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/revisions")
public class RevisionsController {

    private final EntityManagerFactory factory;

    private final ClientMapper clientMapper;

    private final LoanMapper loanMapper;

    @GetMapping("clients/{clientId}")
    public List<ClientResponse> getClientRevisions(@PathVariable String clientId) {
        AuditReader reader = AuditReaderFactory.get(factory.createEntityManager());
        List<Client> resultList = reader.createQuery()
            .forRevisionsOfEntity(Client.class, true, true)
            .add(AuditEntity.id().eq(clientId))
            .getResultList();

        return resultList
            .stream()
            .map(clientMapper::map)
            .toList();
    }

    @GetMapping("loans/{loanId}")
    public List<LoanResponse> getLoansRevisions(@PathVariable long loanId) {
        AuditReader reader = AuditReaderFactory.get(factory.createEntityManager());
        List<Loan> resultList = reader.createQuery()
            .forRevisionsOfEntity(Loan.class, true, true)
            .add(AuditEntity.id().eq(loanId))
            .getResultList();

        return resultList
            .stream()
            .map(loanMapper::map)
            .toList();
    }

}
