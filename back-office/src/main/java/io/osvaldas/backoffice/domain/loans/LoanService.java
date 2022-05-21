package io.osvaldas.backoffice.domain.loans;

import static io.osvaldas.api.clients.Status.ACTIVE;
import static io.osvaldas.api.loans.Status.OPEN;
import static io.osvaldas.api.loans.Status.PENDING;
import static io.osvaldas.api.loans.Status.REJECTED;
import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.of;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.api.exceptions.ClientNotActiveException;
import io.osvaldas.api.exceptions.NotFoundException;
import io.osvaldas.api.exceptions.ValidationRuleException;
import io.osvaldas.api.loans.Status;
import io.osvaldas.api.loans.TodayTakenLoansCount;
import io.osvaldas.api.risk.validation.RiskValidationRequest;
import io.osvaldas.api.risk.validation.RiskValidationResponse;
import io.osvaldas.api.util.TimeUtils;
import io.osvaldas.backoffice.domain.clients.ClientService;
import io.osvaldas.backoffice.infra.configuration.PropertiesConfig;
import io.osvaldas.backoffice.repositories.LoanRepository;
import io.osvaldas.backoffice.repositories.entities.Client;
import io.osvaldas.backoffice.repositories.entities.Loan;
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

    private final RiskCheckerClient riskCheckerClient;

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
    public Loan addLoan(Loan loan, String clientId) {
        log.info("Adding loan for client: {}", clientId);
        Client client = of(getClient(clientId))
            .filter(c -> ACTIVE == c.getStatus())
            .orElseThrow(() -> new ClientNotActiveException(clientNotActiveMessage));
        return addLoanToClient(client, loan);
    }

    @Transactional
    public TodayTakenLoansCount getTodayTakenLoansCount(String clientId) {
        int loansTakenToday = clientService.getLoanTakenTodayCount(clientId, timeUtils.getCurrentDateTime().truncatedTo(DAYS));
        return new TodayTakenLoansCount(loansTakenToday);
    }

    @Transactional
    public void validate(Loan loan, String clientId) {
        log.info("Validating loan: {}", loan.getId());
        try {
            RiskValidationResponse response = riskCheckerClient.validate(new RiskValidationRequest(loan.getId(), clientId));
            log.info("Risk validation response: {}", response);
            of(response)
                .filter(RiskValidationResponse::isSuccess)
                .ifPresentOrElse(r -> {
                    log.info("Success validating loan: {}", loan.getId());
                    setStatusAndSave(loan, OPEN);
                }, () -> rejectLoanAndThrow(loan, "Failed to validate loan risk!"));
        } catch (Exception e) {
            log.error("Error validating loan: {}", loan.getId(), e);
            rejectLoanAndThrow(loan, e.getMessage());
        }
    }

    private Loan addLoanToClient(Client client, Loan loan) {
        cancelPreviousPendingLoan(client);
        loan.setInterestAndReturnDate(config.getInterestRate(), timeUtils.getCurrentDateTime());
        client.addLoan(loan);
        return clientService.save(client).getLastLoan();
    }

    private void cancelPreviousPendingLoan(Client client) {
        of(client)
            .map(Client::getLastLoan)
            .filter(loan -> PENDING == loan.getStatus())
            .ifPresent(loan -> {
                loan.setStatus(REJECTED);
                loanRepository.save(loan);
            });
    }

    private Client getClient(String clientId) {
        return clientService.getClient(clientId);
    }

    private void rejectLoanAndThrow(Loan loan, String message) {
        setStatusAndSave(loan, REJECTED);
        throw new ValidationRuleException(message);
    }

    private void setStatusAndSave(Loan loan, Status open) {
        loan.setStatus(open);
        save(loan);
    }

}
