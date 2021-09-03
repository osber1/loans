package com.finance.loans.domain.clients;

import static com.finance.loans.repositories.Client.buildClientResponse;
import static com.finance.loans.repositories.ClientDAO.buildNewClientDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.finance.loans.domain.exceptions.NotFoundException;
import com.finance.loans.domain.loans.rules.Validator;
import com.finance.loans.domain.util.TimeUtils;
import com.finance.loans.infra.configuration.PropertiesConfig;
import com.finance.loans.repositories.Client;
import com.finance.loans.repositories.ClientDAO;
import com.finance.loans.repositories.ClientRepository;
import com.finance.loans.repositories.Loan;
import com.finance.loans.repositories.LoanPostpone;
import com.finance.loans.repositories.LoanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private static final String LOAN_NOT_EXIST = "Loan with id %s does not exist.";

    private static final String CLIENT_NOT_EXIST = "Client with id %s does not exist.";

    private static final int NUMBERS_AFTER_COMMA = 2;

    private final ClientRepository clientRepository;

    private final LoanRepository loanRepository;

    private final PropertiesConfig config;

    private final Validator validator;

    private final TimeUtils timeUtils;

    @Transactional
    public Client takeLoan(Client client) {
        validateClient(client);

        ClientDAO loanClient = clientRepository.findByPersonalCode(client.getPersonalCode())
            .orElseGet(() -> buildNewClientDAO(client));

        loanClient.addLoan(client.getLoan());
        setNewInterestAndReturnDate(client);
        ClientDAO savedClient = clientRepository.save(loanClient);
        return buildClientResponse(savedClient);
    }

    @Transactional
    public LoanPostpone postponeLoan(long id) {
        Loan loanRequest = getLoan(id);
        loanRequest.getLoanPostpones().add(buildLoanPostpone(loanRequest));
        Loan savedLoanRequest = loanRepository.save(loanRequest);
        return savedLoanRequest.getLoanPostpones().stream().max(Comparator.comparing(LoanPostpone::getNewReturnDate)).get();
    }

    public Collection<Loan> getClientHistory(String id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format(CLIENT_NOT_EXIST, id)))
            .getLoans();
    }

    private Loan getLoan(long id) {
        return loanRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format(LOAN_NOT_EXIST, id)));
    }

    private LoanPostpone buildLoanPostpone(Loan loanRequest) {
        LoanPostpone loanPostpone = new LoanPostpone();
        ZonedDateTime newReturnDate;
        BigDecimal newInterestRate;
        if (loanRequest.getLoanPostpones().isEmpty()) {
            newReturnDate = loanRequest.getReturnDate();
            newInterestRate = loanRequest.getInterestRate();
        } else {
            LoanPostpone latestPostpone = loanRequest.getLoanPostpones().stream().max(Comparator.comparing(LoanPostpone::getNewReturnDate)).get();
            newReturnDate = latestPostpone.getNewReturnDate();
            newInterestRate = latestPostpone.getNewInterestRate();
        }
        loanPostpone.setNewInterestRate(calculateNewInterestRate(newInterestRate)
            .setScale(NUMBERS_AFTER_COMMA, RoundingMode.HALF_UP));
        loanPostpone.setNewReturnDate(calculateNewReturnDate(newReturnDate));
        return loanPostpone;
    }

    private BigDecimal calculateNewInterestRate(BigDecimal newInterestRate) {
        return newInterestRate.multiply(config.getInterestIncrementFactor());
    }

    private ZonedDateTime calculateNewReturnDate(ZonedDateTime newReturnDate) {
        return newReturnDate.plusDays(config.getPostponeDays());
    }

    private void validateClient(Client client) {
        validator.validate(client.getLoan().getAmount());
    }

    private void setNewInterestAndReturnDate(Client client) {
        client.getLoan().setInterestRate(config.getInterestRate());
        client.getLoan().setReturnDate(
            timeUtils.getCurrentDateTime().plusMonths(client.getLoan().getTermInMonths()));
    }
}
