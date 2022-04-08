package io.osvaldas.backoffice.domain.loans;

import static java.lang.String.format;
import static java.util.Comparator.comparing;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.backoffice.domain.clients.ClientService;
import io.osvaldas.backoffice.domain.exceptions.ClientNotActiveException;
import io.osvaldas.backoffice.domain.exceptions.NotFoundException;
import io.osvaldas.backoffice.domain.loans.validators.Validator;
import io.osvaldas.backoffice.domain.util.TimeUtils;
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig;
import io.osvaldas.backoffice.repositories.LoanRepository;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.backoffice.repositories.entities.Loan;
import io.osvaldas.backoffice.repositories.entities.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final ClientService clientService;

    private final LoanRepository loanRepository;

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    private final Validator validator;

    @Value("${exceptionMessages.loanErrorMessage:}")
    private String loanErrorMessage;

    @Value("${exceptionMessages.clientNotActiveMessage:}")
    private String clientNotActiveMessage;

    @Transactional
    public Loan save(Loan loan) {
        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public Loan getLoan(long id) {
        return loanRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(format(loanErrorMessage, id)));
    }

    @Transactional(readOnly = true)
    public Collection<Loan> getLoans(String clientId) {
        return getClient(clientId).getLoans();
    }

    @Transactional
    public Loan takeLoan(Loan loan, String clientId) {
        Client client = getClient(clientId);
        checkIfClientActive(client);
        validate(loan);
        loan.setNewLoanInterestAndReturnDate(config.getInterestRate(), timeUtils.getCurrentDateTime());
        return addLoanToClient(client, loan);
    }

    private void checkIfClientActive(Client client) {
        if (Status.ACTIVE != client.getStatus()) {
            throw new ClientNotActiveException(clientNotActiveMessage);
        }
    }

    private void validate(Loan loan) {
        validator.validate(loan.getAmount());
    }

    private Loan addLoanToClient(Client client, Loan loan) {
        client.addLoan(loan);
        return clientService.save(client)
            .getLoans().stream()
            .max(comparing(Loan::getId))
            .orElse(null);
    }

    private Client getClient(String clientId) {
        return clientService.getClient(clientId);
    }

}
