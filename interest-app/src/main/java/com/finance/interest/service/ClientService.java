package com.finance.interest.service;

import static com.finance.interest.util.TimeUtils.getCurrentDateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.finance.interest.configuration.PropertiesConfig;
import com.finance.interest.exception.NotFoundException;
import com.finance.interest.model.Client;
import com.finance.interest.model.ClientDAO;
import com.finance.interest.model.Loan;
import com.finance.interest.model.LoanPostpone;
import com.finance.interest.repository.ClientRepository;
import com.finance.interest.repository.LoanRepository;
import com.finance.interest.util.ValidationUtils;

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

    private final ValidationUtils validationUtils;

    @Transactional
    public Client takeLoan(Client client, HttpServletRequest request) {
        validationUtils.checkIfAmountIsNotToHigh(client, config.getMaxAmount());
        validationUtils.checkTimeAndAmount(client, config.getMaxAmount(), config.getForbiddenHourFrom(), config.getForbiddenHourTo());
        validationUtils.checkIpAddress(request, config.getRequestsFromSameIpLimit());

        ClientDAO loanClient;
        Optional<ClientDAO> existingClient = clientRepository.findByPersonalCode(client.getPersonalCode());
        if (existingClient.isPresent()) {
            loanClient = existingClient.get();
            loanClient.getLoans().add(client.getLoan());
        } else {
            loanClient = buildNewClientDAO(client);
        }
        client.getLoan().setInterestRate(config.getInterestRate());
        client.getLoan().setReturnDate(getCurrentDateTime().plusMonths(client.getLoan().getTermInMonths()));
        ClientDAO savedClient = clientRepository.save(loanClient);
        return buildClientResponse(savedClient);
    }

    @Transactional
    public LoanPostpone postponeLoan(int id) {
        ZonedDateTime newReturnDate;
        double newInterestRate;
        LoanPostpone loanPostpone = new LoanPostpone();
        Loan loanRequest = getLoan(id);

        Set<LoanPostpone> loanPostpones = loanRequest.getLoanPostpones();
        Comparator<LoanPostpone> latestLoanPostpone = Comparator.comparing(LoanPostpone::getNewReturnDate);

        if (loanPostpones == null || loanPostpones.isEmpty()) {
            loanPostpones = new HashSet<>();
            newReturnDate = loanRequest.getReturnDate().plusDays(config.getPostponeDays());
            newInterestRate = loanRequest.getInterestRate() * config.getInterestIncrementFactor();
        } else {
            LoanPostpone firstPostpone = loanPostpones.stream().max(latestLoanPostpone).get();
            newReturnDate = firstPostpone.getNewReturnDate().plusDays(config.getPostponeDays());
            newInterestRate = firstPostpone.getNewInterestRate() * config.getInterestIncrementFactor();
        }
        loanPostpone.setNewInterestRate(roundNewInterestRate(newInterestRate));
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

    private double roundNewInterestRate(double newInterestRate) {
        return BigDecimal.valueOf(newInterestRate).setScale(NUMBERS_AFTER_COMMA, RoundingMode.HALF_UP).doubleValue();
    }

    private Client buildClientResponse(ClientDAO savedClient) {
        return Client.builder()
            .id(savedClient.getId())
            .firstName(savedClient.getFirstName())
            .lastName(savedClient.getLastName())
            .personalCode(savedClient.getPersonalCode())
            .loan(savedClient.getLoans().stream().max(Comparator.comparing(Loan::getId)).get())
            .createdAt(savedClient.getCreatedAt())
            .updatedAt(savedClient.getUpdatedAt()).build();
    }

    private ClientDAO buildNewClientDAO(Client client) {
        return ClientDAO.builder()
            .id(UUID.randomUUID().toString())
            .firstName(client.getFirstName())
            .lastName(client.getLastName())
            .personalCode(client.getPersonalCode())
            .loans(Collections.singleton(client.getLoan())).build();
    }
}
