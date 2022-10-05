package io.osvaldas.backoffice.domain.loans;

import static io.osvaldas.api.clients.Status.ACTIVE;
import static io.osvaldas.api.loans.Status.OPEN;
import static io.osvaldas.api.loans.Status.PENDING;
import static io.osvaldas.api.loans.Status.REJECTED;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.clientIdIs;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanCreationDateIsAfter;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanStatusIs;
import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Optional.of;
import static org.springframework.data.jpa.domain.Specification.where;

import java.time.ZonedDateTime;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
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

    @Value("${exceptionMessages.loanNotFound:}")
    private String loanNotFound;

    @Value("${exceptionMessages.clientNotActive:}")
    private String clientNotActive;

    @Value("${exceptionMessages.validationRequestFailed:}")
    private String validationRequestFailed;

    @Transactional
    public Loan save(Loan loan) {
        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public Loan getLoan(long id) {
        return loanRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(format(loanNotFound, id)));
    }

    @Transactional(readOnly = true)
    public Collection<Loan> getLoans(String clientId) {
        return getClient(clientId).getLoans();
    }

    @Transactional
    public Loan addLoan(Loan loan, String clientId) {
        log.info("Adding loan for client: {}", clientId);
        Client client = getActiveClient(clientId);
        cancelPreviousPendingLoan(client);
        return addLoanToClient(client, loan);
    }

    @Transactional(readOnly = true)
    public TodayTakenLoansCount getTodayTakenLoansCount(String clientId) {
        int loansTakenToday = getLoanTakenTodayCount(clientId, timeUtils.getCurrentDateTime().truncatedTo(DAYS));
        return new TodayTakenLoansCount(loansTakenToday);
    }

    @Transactional(noRollbackForClassName = { "ValidationRuleException" })
    public void validate(Loan loan, String clientId) {
        RiskValidationResponse response = sendValidationRequest(loan, clientId);
        of(response)
            .filter(RiskValidationResponse::isSuccess)
            .ifPresentOrElse(r -> approveAndSave(loan),
                () -> rejectLoanAndThrow(loan, response.getMessage()));
    }

    private Client getActiveClient(String clientId) {
        return of(getClient(clientId))
            .filter(c -> ACTIVE == c.getStatus())
            .orElseThrow(() -> new ClientNotActiveException(clientNotActive));
    }

    private RiskValidationResponse sendValidationRequest(Loan loan, String clientId) {
        try {
            log.info("Validating loan: {}", loan.getId());
            RiskValidationResponse response = riskCheckerClient.validate(new RiskValidationRequest(loan.getId(), clientId));
            log.info("Risk validation response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error validating loan: {}", loan.getId(), e);
        }
        return new RiskValidationResponse(false, validationRequestFailed);
    }

    private int getLoanTakenTodayCount(String clientId, ZonedDateTime date) {
        Specification<Loan> specification = where(
            clientIdIs(clientId)
                .and(loanCreationDateIsAfter(date))
                .and(loanStatusIs(OPEN)));
        return loanRepository.findAll(specification).size();
    }

    private Loan addLoanToClient(Client client, Loan loan) {
        loan.setInterestAndReturnDate(config.getInterestRate(), timeUtils.getCurrentDateTime());
        loan.setClient(client);
        return loanRepository.save(loan);
    }

    private void cancelPreviousPendingLoan(Client client) {
        of(client)
            .flatMap(Client::getLastLoan)
            .filter(loan -> PENDING == loan.getStatus())
            .ifPresent(loan -> setStatusAndSave(loan, REJECTED));
    }

    private Client getClient(String clientId) {
        return clientService.getClient(clientId);
    }

    private void approveAndSave(Loan loan) {
        log.info("Success validating loan: {}", loan.getId());
        setStatusAndSave(loan, OPEN);
    }

    private void rejectLoanAndThrow(Loan loan, String message) {
        setStatusAndSave(loan, REJECTED);
        throw new ValidationRuleException(message);
    }

    private void setStatusAndSave(Loan loan, Status status) {
        loan.setStatus(status);
        save(loan);
    }

}
