package com.finance.interest.service;

import static com.finance.interest.model.Client.buildClientResponse;
import static com.finance.interest.model.ClientDAO.buildNewClientDAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.finance.interest.configuration.PropertiesConfig;
import com.finance.interest.exception.NotFoundException;
import com.finance.interest.interfaces.TimeUtils;
import com.finance.interest.interfaces.Validator;
import com.finance.interest.model.Client;
import com.finance.interest.model.ClientDAO;
import com.finance.interest.model.Loan;
import com.finance.interest.model.LoanPostpone;
import com.finance.interest.repository.ClientRepository;
import com.finance.interest.repository.LoanRepository;

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
    public Client takeLoan(Client client, String ip) {
        validator.validate(ip, client.getLoan().getAmount());

        ClientDAO loanClient = clientRepository.findByPersonalCode(client.getPersonalCode())
            .orElse(buildNewClientDAO(client));

        if (loanClient.getLoans() == null) {
            loanClient.setLoans(new HashSet<>());
        }
        loanClient.getLoans().add(client.getLoan());

        client.getLoan().setInterestRate(config.getInterestRate());
        client.getLoan().setReturnDate(timeUtils.getCurrentDateTime().plusMonths(client.getLoan().getTermInMonths()));
        ClientDAO savedClient = clientRepository.save(loanClient);
        return buildClientResponse(savedClient);
    }

    @Transactional
    public LoanPostpone postponeLoan(int id) {
        ZonedDateTime newReturnDate;
        BigDecimal newInterestRate;
        LoanPostpone loanPostpone = new LoanPostpone();
        Loan loanRequest = getLoan(id);

        Set<LoanPostpone> loanPostpones = loanRequest.getLoanPostpones();
        Comparator<LoanPostpone> latestLoanPostpone = Comparator.comparing(LoanPostpone::getNewReturnDate);

        if (loanPostpones == null || loanPostpones.isEmpty()) {
            loanPostpones = new HashSet<>();
            newReturnDate = loanRequest.getReturnDate().plusDays(config.getPostponeDays());
            newInterestRate = loanRequest.getInterestRate().multiply(config.getInterestIncrementFactor());
        } else {
            LoanPostpone firstPostpone = loanPostpones.stream().max(latestLoanPostpone).get();
            newReturnDate = firstPostpone.getNewReturnDate().plusDays(config.getPostponeDays());
            newInterestRate = firstPostpone.getNewInterestRate().multiply(config.getInterestIncrementFactor());
        }
        loanPostpone.setNewInterestRate(newInterestRate.setScale(NUMBERS_AFTER_COMMA, RoundingMode.HALF_UP));
        loanPostpone.setNewReturnDate(newReturnDate);
        loanPostpones.add(loanPostpone);
        loanRequest.setLoanPostpones(loanPostpones);
        Loan savedLoanRequest = loanRepository.save(loanRequest);
        return savedLoanRequest.getLoanPostpones().stream().max(latestLoanPostpone).get();
    }

    private Loan getLoan(int id) {
        return loanRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format(LOAN_NOT_EXIST, id)));
    }

    public Collection<Loan> getClientHistory(String id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(String.format(CLIENT_NOT_EXIST, id)))
            .getLoans();
    }
}
