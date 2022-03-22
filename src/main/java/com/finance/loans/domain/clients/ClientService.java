package com.finance.loans.domain.clients;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.loans.domain.exceptions.NotFoundException;
import com.finance.loans.domain.loans.validators.Validator;
import com.finance.loans.domain.util.TimeUtils;
import com.finance.loans.infra.configuration.PropertiesConfig;
import com.finance.loans.repositories.ClientRepository;
import com.finance.loans.repositories.LoanRepository;
import com.finance.loans.repositories.entities.Client;
import com.finance.loans.repositories.entities.Loan;
import com.finance.loans.repositories.entities.LoanPostpone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.lang.String.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private static final String CLIENT_NOT_EXIST = "Client with id %s does not exist.";

    private static final String LOAN_NOT_EXIST = "Loan with id %s does not exist.";

    private static final int NUMBERS_AFTER_COMMA = 2;

    private final ClientRepository clientRepository;

    private final LoanRepository loanRepository;

    private final PropertiesConfig config;

    private final Validator validator;

    private final TimeUtils timeUtils;

    @Transactional
    public Client registerClient(Client client) {
        client.setId(UUID.randomUUID().toString());
        return clientRepository.save(client);
    }

    @Transactional(readOnly = true)
    public Collection<Client> getClients() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Client getClient(String id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(format(CLIENT_NOT_EXIST, id)));
    }

    @Transactional
    public void deleteClient(String id) {
        if (!clientRepository.existsById(id)) {
            throw new NotFoundException(format(CLIENT_NOT_EXIST, id));
        }
        clientRepository.deleteById(id);
    }

    @Transactional
    public Client updateClient(Client client) {
        if (!clientRepository.existsById(client.getId())) {
            throw new NotFoundException(format(CLIENT_NOT_EXIST, client.getId()));
        }
        return clientRepository.save(client);
    }

    @Transactional
    public Loan takeLoan(Loan loan, String id) {
        validateLoan(loan);
        Client client = getClient(id);
        setNewInterestAndReturnDate(loan);
        client.addLoan(loan);
        return clientRepository.save(client).getLoans().stream().max(Comparator.comparing(Loan::getId)).orElse(null);
    }

    @Transactional
    public LoanPostpone postponeLoan(long id) {
        Loan loanRequest = getLoan(id);
        loanRequest.getLoanPostpones().add(buildLoanPostpone(loanRequest));
        Loan savedLoanRequest = loanRepository.save(loanRequest);
        return savedLoanRequest.getLoanPostpones().stream().max(Comparator.comparing(LoanPostpone::getNewReturnDate)).orElse(null);
    }

    public Collection<Loan> getClientHistory(String id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(format(CLIENT_NOT_EXIST, id)))
            .getLoans();
    }

    private Loan getLoan(long id) {
        return loanRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(format(LOAN_NOT_EXIST, id)));
    }

    private LoanPostpone buildLoanPostpone(Loan loanRequest) {
        LoanPostpone loanPostpone = new LoanPostpone();
        ZonedDateTime newReturnDate;
        BigDecimal newInterestRate;
        if (loanRequest.getLoanPostpones().isEmpty()) {
            newReturnDate = loanRequest.getReturnDate();
            newInterestRate = loanRequest.getInterestRate();
        } else {
            LoanPostpone latestPostpone = loanRequest.getLoanPostpones().stream().max(Comparator.comparing(LoanPostpone::getNewReturnDate)).orElse(null);
            newReturnDate = latestPostpone.getNewReturnDate();
            newInterestRate = latestPostpone.getNewInterestRate();
        }
        loanPostpone.setNewInterestRate(calculateNewInterestRate(newInterestRate).setScale(NUMBERS_AFTER_COMMA, RoundingMode.HALF_UP));
        loanPostpone.setNewReturnDate(calculateNewReturnDate(newReturnDate));
        return loanPostpone;
    }

    private BigDecimal calculateNewInterestRate(BigDecimal newInterestRate) {
        return newInterestRate.multiply(config.getInterestIncrementFactor());
    }

    private ZonedDateTime calculateNewReturnDate(ZonedDateTime newReturnDate) {
        return newReturnDate.plusDays(config.getPostponeDays());
    }

    private void validateLoan(Loan loan) {
        validator.validate(loan.getAmount());
    }

    private void setNewInterestAndReturnDate(Loan loan) {
        loan.setInterestRate(config.getInterestRate());
        loan.setReturnDate(timeUtils.getCurrentDateTime().plusMonths(loan.getTermInMonths()));
    }
}
