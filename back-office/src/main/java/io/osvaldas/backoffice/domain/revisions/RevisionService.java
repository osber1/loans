package io.osvaldas.backoffice.domain.revisions;

import java.util.List;

import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.api.clients.ClientResponse;
import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.backoffice.repositories.ClientRepository;
import io.osvaldas.backoffice.repositories.LoanRepository;
import io.osvaldas.backoffice.repositories.mapper.ClientMapper;
import io.osvaldas.backoffice.repositories.mapper.LoanMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevisionService {

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    private final LoanRepository loanRepository;

    private final LoanMapper loanMapper;

    public List<ClientResponse> getClientRevisions(String clientId) {
        return clientRepository.findRevisions(clientId).stream()
            .map(Revision::getEntity)
            .map(clientMapper::map)
            .toList();
    }

    public List<LoanResponse> getLoanRevisions(long loanId) {
        return loanRepository.findRevisions(loanId).stream()
            .map(Revision::getEntity)
            .map(loanMapper::map)
            .toList();
    }

}
