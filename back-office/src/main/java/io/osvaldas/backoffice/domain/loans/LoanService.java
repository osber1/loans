package io.osvaldas.backoffice.domain.loans;

import static io.osvaldas.api.clients.Status.ACTIVE;
import static io.osvaldas.api.loans.Status.NOT_EVALUATED;
import static io.osvaldas.api.loans.Status.OPEN;
import static io.osvaldas.api.loans.Status.PENDING;
import static io.osvaldas.api.loans.Status.REJECTED;
import static io.osvaldas.api.util.ExceptionMessages.CLIENT_NOT_ACTIVE;
import static io.osvaldas.api.util.ExceptionMessages.LOAN_NOT_FOUND;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.clientIdIs;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanCreatedAtOrAfter;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanStatusIs;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import io.osvaldas.api.exceptions.BadRequestException;
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

    static final String LOAN_STATUS_CHANGED = "Loan with id %s has status %s, expected one of %s.";

    /**
     * Statuses from which a loan may be (re-)evaluated: a freshly taken loan is {@code PENDING},
     * a loan whose previous evaluation failed (e.g. risk-checker unavailable) is {@code NOT_EVALUATED}.
     */
    private static final Set<Status> EVALUABLE_STATUSES = EnumSet.of(PENDING, NOT_EVALUATED);

    private final ClientService clientService;

    private final LoanRepository loanRepository;

    private final PropertiesConfig config;

    private final TimeUtils timeUtils;

    private final RiskCheckerClient riskCheckerClient;

    private final TransactionTemplate transactionTemplate;

    @Transactional
    public Loan save(Loan loan) {
        return loanRepository.save(loan);
    }

    @Transactional(readOnly = true)
    public Loan getLoan(long id) {
        return loanRepository.findWithPostponesById(id)
            .orElseThrow(() -> new NotFoundException(LOAN_NOT_FOUND.formatted(id)));
    }

    /**
     * Loads the loan with a row lock, so that concurrent modifications (e.g. postpones) are serialized.
     */
    @Transactional(propagation = MANDATORY)
    public Loan getLoanForUpdate(long id) {
        return loanRepository.findForUpdateById(id)
            .orElseThrow(() -> new NotFoundException(LOAN_NOT_FOUND.formatted(id)));
    }

    @Transactional(readOnly = true)
    public Collection<Loan> getLoans(String clientId) {
        Client client = getClient(clientId);
        return loanRepository.findAllWithPostponesByClientId(client.getId());
    }

    @Transactional
    public Loan addLoan(Loan loan, String clientId) {
        log.info("Adding loan for client: {}", clientId);
        Client client = getActiveClient(clientId);
        rejectPreviousPendingLoan(clientId);
        return addLoanToClient(client, loan);
    }

    @Transactional(readOnly = true)
    public TodayTakenLoansCount getTodayTakenLoansCount(String clientId) {
        long loansTakenToday = getLoanTakenTodayCount(clientId, timeUtils.getCurrentDateTime().truncatedTo(DAYS));
        return new TodayTakenLoansCount(Math.toIntExact(loansTakenToday));
    }

    /**
     * Intentionally not transactional: the remote risk validation must not hold a database transaction.
     * Each status transition runs in its own short transaction instead.
     */
    public void validate(Loan loan, String clientId) {
        changeStatus(loan, EVALUABLE_STATUSES, NOT_EVALUATED);
        RiskValidationResponse response = sendValidationRequest(loan, clientId);
        Optional.of(response)
            .filter(RiskValidationResponse::success)
            .ifPresentOrElse(r -> approve(loan),
                () -> rejectLoanAndThrow(loan, response.message()));
    }

    @Transactional(readOnly = true)
    public List<Loan> getLoansByStatus(Status status) {
        return loanRepository.findAllWithClientByStatus(status);
    }

    private Client getActiveClient(String clientId) {
        return Optional.of(getClient(clientId))
            .filter(c -> ACTIVE == c.getStatus())
            .orElseThrow(() -> new ClientNotActiveException(CLIENT_NOT_ACTIVE));
    }

    private RiskValidationResponse sendValidationRequest(Loan loan, String clientId) {
        try {
            log.info("Validating loan: {}", loan.getId());
            RiskValidationResponse response = riskCheckerClient.validate(new RiskValidationRequest(loan.getId(), clientId));
            log.info("Risk validation response: {}", response);
            return response;
        } catch (RuntimeException e) {
            log.error("Error validating loan: {}", loan.getId());
            throw e;
        }
    }

    private long getLoanTakenTodayCount(String clientId, ZonedDateTime startOfDay) {
        Specification<Loan> specification = clientIdIs(clientId)
            .and(loanCreatedAtOrAfter(startOfDay))
            .and(loanStatusIs(OPEN));
        return loanRepository.count(specification);
    }

    private Loan addLoanToClient(Client client, Loan loan) {
        loan.setInterestAndReturnDate(config.getInterestRate(), timeUtils.getCurrentDateTime());
        loan.setClient(client);
        return loanRepository.save(loan);
    }

    private void rejectPreviousPendingLoan(String clientId) {
        loanRepository.findFirstByClientIdOrderByIdDesc(clientId)
            .filter(loan -> PENDING == loan.getStatus())
            .ifPresent(loan -> {
                log.info("Rejecting previous pending loan: {}", loan.getId());
                loan.setStatus(REJECTED);
            });
    }

    private Client getClient(String clientId) {
        return clientService.getClient(clientId);
    }

    private void approve(Loan loan) {
        log.info("Success validating loan: {}", loan.getId());
        changeStatus(loan, Set.of(NOT_EVALUATED), OPEN);
    }

    private void rejectLoanAndThrow(Loan loan, String message) {
        changeStatus(loan, Set.of(NOT_EVALUATED), REJECTED);
        throw new ValidationRuleException(message);
    }

    /**
     * Guarded status transition: re-reads the loan in its own transaction, verifies it is still in one of the
     * expected statuses and updates the managed entity (optimistic locking via {@code @Version}, audited).
     * The passed (possibly detached) instance is never merged, only its in-memory status is kept in sync.
     */
    private void changeStatus(Loan loan, Set<Status> expectedStatuses, Status newStatus) {
        long id = loan.getId();
        transactionTemplate.executeWithoutResult(tx -> {
            Loan managedLoan = loanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(LOAN_NOT_FOUND.formatted(id)));
            Status currentStatus = managedLoan.getStatus();
            if (!expectedStatuses.contains(currentStatus)) {
                throw new BadRequestException(LOAN_STATUS_CHANGED.formatted(id, currentStatus, expectedStatuses));
            }
            managedLoan.setStatus(newStatus);
        });
        loan.setStatus(newStatus);
    }

}
