package io.osvaldas.backoffice.domain.loans;

import static io.osvaldas.api.clients.Status.ACTIVE;
import static io.osvaldas.api.loans.Status.NOT_EVALUATED;
import static io.osvaldas.api.loans.Status.OPEN;
import static io.osvaldas.api.loans.Status.REJECTED;
import static io.osvaldas.api.util.ExceptionMessages.CLIENT_NOT_ACTIVE;
import static io.osvaldas.api.util.ExceptionMessages.LOAN_NOT_FOUND;
import static io.osvaldas.backoffice.infra.configuration.BeansConfig.LOAN_RESPONSE_CACHE;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.clientIdIs;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanCreatedAtOrAfter;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanCreatedBefore;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanIdLessThan;
import static io.osvaldas.backoffice.repositories.specifications.LoanSpecifications.loanStatusIn;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.transaction.annotation.Propagation.MANDATORY;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.osvaldas.api.exceptions.ClientNotActiveException;
import io.osvaldas.api.exceptions.NotFoundException;
import io.osvaldas.api.exceptions.ValidationRuleException;
import io.osvaldas.api.loans.Status;
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

    private final CacheManager cacheManager;

    @Transactional(readOnly = true)
    public Loan getLoan(long id) {
        return loanRepository.findWithPostponesById(id)
            .orElseThrow(() -> new NotFoundException(LOAN_NOT_FOUND.formatted(id)));
    }

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
        return addLoanToClient(client, loan);
    }

    public void validate(Loan loan, String clientId) {
        try {
            RiskValidationResponse response = sendValidationRequest(loan, clientId);
            Optional.of(response)
                .filter(RiskValidationResponse::success)
                .ifPresentOrElse(r -> approveAndSave(loan),
                    () -> rejectLoanAndThrow(loan, response));
        } finally {
            Optional.ofNullable(cacheManager.getCache(LOAN_RESPONSE_CACHE))
                .ifPresent(cache -> cache.evict(loan.getId()));
        }
    }

    public List<Loan> getLoansByStatusOlderThan(Status status, Duration minAge, long afterId, int limit) {
        return loanRepository.findAllWithClientByStatusAndCreatedAtBeforeAndIdGreaterThanOrderByIdAsc(
            status, timeUtils.getCurrentDateTime().minus(minAge), afterId, Limit.of(limit));
    }

    private Client getActiveClient(String clientId) {
        return Optional.of(clientService.getClientForUpdate(clientId))
            .filter(c -> ACTIVE == c.getStatus())
            .orElseThrow(() -> new ClientNotActiveException(CLIENT_NOT_ACTIVE));
    }

    private RiskValidationResponse sendValidationRequest(Loan loan, String clientId) {
        ZonedDateTime requestedAt = requestedAt(loan);
        long loansTakenToday = getLoanTakenTodayCount(clientId, loan.getId(), requestedAt);
        log.info("Validating loan: {}", loan.getId());
        RiskValidationResponse response = riskCheckerClient.validate(
            new RiskValidationRequest(loan.getId(), clientId, loan.getAmount(), loansTakenToday, requestedAt));
        log.info("Risk validation response: {}", response);
        return response;
    }

    private ZonedDateTime requestedAt(Loan loan) {
        ZonedDateTime now = timeUtils.getCurrentDateTime();
        return Optional.ofNullable(loan.getCreatedAt())
            .map(createdAt -> createdAt.withZoneSameInstant(now.getZone()))
            .orElse(now);
    }

    private long getLoanTakenTodayCount(String clientId, long loanId, ZonedDateTime requestedAt) {
        ZonedDateTime startOfDay = requestedAt.truncatedTo(DAYS);
        Specification<Loan> specification = clientIdIs(clientId)
            .and(loanCreatedAtOrAfter(startOfDay))
            .and(loanCreatedBefore(startOfDay.plusDays(1)))
            .and(loanStatusIn(EnumSet.of(NOT_EVALUATED, OPEN)))
            .and(loanIdLessThan(loanId));
        return loanRepository.count(specification);
    }

    private Loan addLoanToClient(Client client, Loan loan) {
        loan.setInterestAndReturnDate(config.getInterestRate(), timeUtils.getCurrentDateTime());
        loan.setClient(client);
        return loanRepository.save(loan);
    }

    private Client getClient(String clientId) {
        return clientService.getClient(clientId);
    }

    private void approveAndSave(Loan loan) {
        log.info("Success validating loan: {}", loan.getId());
        setStatusAndSave(loan, OPEN);
    }

    private void rejectLoanAndThrow(Loan loan, RiskValidationResponse response) {
        setStatusAndSave(loan, REJECTED);
        throw new ValidationRuleException(response.reason(), response.message());
    }

    private void setStatusAndSave(Loan loan, Status status) {
        loan.setStatus(status);
        loanRepository.save(loan);
    }

}
