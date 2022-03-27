package io.osvaldas.loans.domain.loans;

import static java.lang.String.format;
import static java.util.Comparator.comparing;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.loans.domain.clients.ClientService;
import io.osvaldas.loans.domain.exceptions.NotFoundException;
import io.osvaldas.loans.domain.loans.validators.Validator;
import io.osvaldas.loans.domain.util.TimeUtils;
import io.osvaldas.loans.infra.configuration.PropertiesConfig;
import io.osvaldas.loans.repositories.LoanRepository;
import io.osvaldas.loans.repositories.entities.Client;
import io.osvaldas.loans.repositories.entities.Loan;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LoanService {

    private final ClientService clientService;

    private final LoanRepository loanRepository;

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    private final Validator validator;

    private final String loanErrorMessage;

    public LoanService(ClientService clientService,
                       LoanRepository loanRepository,
                       PropertiesConfig config,
                       TimeUtils timeUtils,
                       Validator validator,
                       @Value("${exceptionMessages.loanErrorMessage:}") String loanErrorMessage) {
        this.clientService = clientService;
        this.loanRepository = loanRepository;
        this.config = config;
        this.timeUtils = timeUtils;
        this.validator = validator;
        this.loanErrorMessage = loanErrorMessage;
    }

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
        validate(loan);
        loan.setNewLoanInterestAndReturnDate(config, timeUtils);
        return addLoanToClient(clientId, loan);
    }

    private void validate(Loan loan) {
        validator.validate(loan.getAmount());
    }

    private Loan addLoanToClient(String clientId, Loan loan) {
        Client client = getClient(clientId);
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
