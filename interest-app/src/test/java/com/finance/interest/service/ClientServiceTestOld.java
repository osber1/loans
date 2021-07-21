package com.finance.interest.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import com.finance.interest.configuration.PropertiesConfig;
import com.finance.interest.exception.BadRequestException;
import com.finance.interest.exception.NotFoundException;
import com.finance.interest.model.Client;
import com.finance.interest.model.ClientDAO;
import com.finance.interest.model.IpLogs;
import com.finance.interest.model.Loan;
import com.finance.interest.model.LoanPostpone;
import com.finance.interest.repository.ClientRepository;
import com.finance.interest.repository.IpLogsRepository;
import com.finance.interest.repository.LoanRepository;
import com.finance.interest.service.ClientService;
import com.finance.interest.util.TimeUtils;

@ExtendWith(MockitoExtension.class)
class ClientServiceTestOld {

    public static final String TIME_ZONE = "Europe/Vilnius";

    public static final ZonedDateTime DATE_TIME_OF_JAN_1_2021 = ZonedDateTime.of(2021, 1, 1, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

    public static final ZonedDateTime DATE_TIME_OF_JAN_8_2021 = ZonedDateTime.of(2021, 1, 8, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

    public static final ZonedDateTime DATE_TIME_OF_JAN_15_2021 = ZonedDateTime.of(2021, 1, 15, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

    public static final ZonedDateTime DATE_OF_DEC_30_2020 = ZonedDateTime.of(2020, 12, 30, 0, 0, 0, 0, ZoneId.of(TIME_ZONE));

    public static final ZonedDateTime DATE_OF_JAN_1_2021 = ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of(TIME_ZONE));

    public static final ZonedDateTime DATE_TIME_OF_JAN_1_2022 = ZonedDateTime.of(2022, 1, 1, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

    private static Client successClient;

    private static Client failedClient;

    private static ClientDAO clientFromRepo;

    private static LoanPostpone firstLoanPostpone;

    private static LoanPostpone secondLoanPostpone;

    private static Loan successfulLoan;

    private static Loan loanWithPostpone;

    private static Loan loanWithTwoPostpones;

    private static IpLogs ipLog;

    private static IpLogs ipLogAfter;

    private static IpLogs ipLogWithLimit;

    private static MockHttpServletRequest httpServletRequest;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private IpLogsRepository clientsIpsRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private TimeUtils timeUtils;

    @Mock
    private PropertiesConfig config;

    @InjectMocks
    private ClientService underTest;

    @BeforeAll
    static void init() {
        successfulLoan = createLoan(DATE_TIME_OF_JAN_1_2022, BigDecimal.valueOf(100));
        firstLoanPostpone = createLoanPostpone(DATE_TIME_OF_JAN_8_2021, 15);
        secondLoanPostpone = createLoanPostpone(DATE_TIME_OF_JAN_15_2021, 22.5);
        loanWithPostpone = createLoanWithPostpone(createLoan(DATE_TIME_OF_JAN_1_2022, BigDecimal.valueOf(100)), firstLoanPostpone);
        loanWithTwoPostpones = createLoanWithPostpones(createLoan(DATE_TIME_OF_JAN_1_2022, BigDecimal.valueOf(100)), firstLoanPostpone, secondLoanPostpone);
        successClient = createClient(successfulLoan);
        failedClient = createClient(createLoan(null, BigDecimal.valueOf(1000)));
        clientFromRepo = createClientDao(successfulLoan);
        ipLog = createIpLog(DATE_TIME_OF_JAN_1_2021, 1);
        ipLogWithLimit = createIpLog(DATE_TIME_OF_JAN_1_2021, 3);
        ipLogAfter = createIpLog(DATE_OF_DEC_30_2020, 1);
        httpServletRequest = createServletRequest();
    }

    private static LoanPostpone createLoanPostpone(ZonedDateTime date, double interestRate) {
        LoanPostpone loanPostpone = new LoanPostpone();
        loanPostpone.setNewReturnDate(date);
        loanPostpone.setNewInterestRate(interestRate);
        return loanPostpone;
    }

    private static MockHttpServletRequest createServletRequest() {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRemoteAddr("0.0.0.0.0");
        return httpServletRequest;
    }

    private static IpLogs createIpLog(ZonedDateTime date, int timesUsed) {
        return IpLogs.builder()
            .id(1)
            .ip("0.0.0.0.1")
            .timesUsed(timesUsed)
            .firstRequestDate(date)
            .build();
    }

    private static ClientDAO createClientDao(Loan loanRequest) {
        return ClientDAO.builder()
            .id("ID-TO-TEST")
            .firstName("Test")
            .lastName("User")
            .personalCode(11111111111L)
            .loans(new HashSet<>(Collections.singletonList(loanRequest))).build();
    }

    private static Client createClient(Loan loanRequest) {
        return Client.builder()
            .id("ID-TO-TEST")
            .firstName("Test")
            .lastName("User")
            .personalCode(11111111111L)
            .loan(loanRequest).build();
    }

    private static Loan createLoan(ZonedDateTime date, BigDecimal amount) {
        Loan successLoan = new Loan();
        successLoan.setId(1);
        successLoan.setInterestRate(10.0);
        successLoan.setTermInMonths(12);
        successLoan.setAmount(amount);
        successLoan.setReturnDate(date);
        return successLoan;
    }

    private static Loan createLoanWithPostpone(Loan loanRequest, LoanPostpone postpone1) {
        List<LoanPostpone> list = Collections.singletonList(postpone1);
        loanRequest.setLoanPostpones(new HashSet<>(list));
        return loanRequest;
    }

    private static Loan createLoanWithPostpones(Loan loanRequest, LoanPostpone postpone1, LoanPostpone postpone2) {
        List<LoanPostpone> list = Arrays.asList(postpone1, postpone2);
        loanRequest.setLoanPostpones(new HashSet<>(list));
        return loanRequest;
    }

    @Test
    void takeLoan_whenNewUser() {
        // given
        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));
        when(config.getInterestRate()).thenReturn(10.0);
        when(config.getRequestsFromSameIpLimit()).thenReturn(3);
        when(clientsIpsRepository.findByIp(anyString())).thenReturn(Optional.of(ipLog));
        when(clientRepository.save(any(ClientDAO.class))).thenReturn(clientFromRepo);

        // when
        Client actualRequest;
        try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
            utilities.when(TimeUtils::getHourOfDay).thenReturn(7);
            utilities.when(TimeUtils::getCurrentDateTime).thenReturn(DATE_TIME_OF_JAN_1_2021);
            utilities.when(TimeUtils::getDayOfMonth).thenReturn(DATE_OF_JAN_1_2021);
            actualRequest = underTest.takeLoan(successClient, httpServletRequest);
        }

        // then
        assertThat(actualRequest).usingRecursiveComparison().isEqualTo(successClient);
    }

    @Test
    void takeLoan_whenExistingUser() {
        // given
        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));
        when(config.getInterestRate()).thenReturn(10.0);
        when(config.getRequestsFromSameIpLimit()).thenReturn(3);
        when(clientsIpsRepository.findByIp(anyString())).thenReturn(Optional.of(ipLog));
        when(clientRepository.save(any(ClientDAO.class))).thenReturn(clientFromRepo);
        when(clientRepository.findByPersonalCode(anyLong())).thenReturn(Optional.of(clientFromRepo));

        // when
        Client actualRequest;
        try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
            utilities.when(TimeUtils::getHourOfDay).thenReturn(7);
            utilities.when(TimeUtils::getCurrentDateTime).thenReturn(DATE_TIME_OF_JAN_1_2021);
            utilities.when(TimeUtils::getDayOfMonth).thenReturn(DATE_OF_JAN_1_2021);
            actualRequest = underTest.takeLoan(successClient, httpServletRequest);
        }

        // then
        assertThat(actualRequest).usingRecursiveComparison().isEqualTo(successClient);
    }

    @Test
    void takeLoan_whenForbiddenTimeAndMaxAmount() {
        // given
        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));
        when(config.getForbiddenHourFrom()).thenReturn(0);
        when(config.getForbiddenHourTo()).thenReturn(6);

        // when
        try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
            utilities.when(TimeUtils::getHourOfDay).thenReturn(3);
            assertThatThrownBy(() -> underTest.takeLoan(successClient, httpServletRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!");
        }
    }

    @Test
    void takeLoan_whenIpLimitExceeded() {
        // given
        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));
        when(config.getRequestsFromSameIpLimit()).thenReturn(1);
        when(clientsIpsRepository.findByIp(anyString())).thenReturn(Optional.of(ipLogWithLimit));

        // when
        try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
            utilities.when(TimeUtils::getHourOfDay).thenReturn(7);
            utilities.when(TimeUtils::getDayOfMonth).thenReturn(DATE_OF_JAN_1_2021);
            assertThatThrownBy(() -> underTest.takeLoan(successClient, httpServletRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Too many requests from the same ip per day.");
        }
    }

    @Test
    void takeLoan_whenIpLimitObjectIsNull() {
        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));
        when(config.getInterestRate()).thenReturn(10.0);
        when(clientsIpsRepository.findByIp(anyString())).thenReturn(Optional.empty());
        when(clientRepository.save(any(ClientDAO.class))).thenReturn(clientFromRepo);

        // when
        Client actualRequest;
        try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
            utilities.when(TimeUtils::getHourOfDay).thenReturn(7);
            utilities.when(TimeUtils::getCurrentDateTime).thenReturn(DATE_TIME_OF_JAN_1_2021);
            utilities.when(TimeUtils::getDayOfMonth).thenReturn(DATE_OF_JAN_1_2021);
            actualRequest = underTest.takeLoan(successClient, httpServletRequest);
        }

        // then
        assertThat(actualRequest).usingRecursiveComparison().isEqualTo(successClient);
    }

    @Test
    void takeLoan_whenIpLimitExceededButItIsNextDay() {
        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));
        when(config.getInterestRate()).thenReturn(10.0);
        when(config.getRequestsFromSameIpLimit()).thenReturn(1);
        when(clientsIpsRepository.findByIp(anyString())).thenReturn(Optional.of(ipLogAfter));
        when(clientRepository.save(any(ClientDAO.class))).thenReturn(clientFromRepo);

        // when
        Client actualRequest;
        try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
            utilities.when(TimeUtils::getHourOfDay).thenReturn(7);
            utilities.when(TimeUtils::getCurrentDateTime).thenReturn(DATE_TIME_OF_JAN_1_2021);
            utilities.when(TimeUtils::getDayOfMonth).thenReturn(DATE_OF_JAN_1_2021);
            actualRequest = underTest.takeLoan(successClient, httpServletRequest);
        }

        // then
        assertThat(actualRequest).usingRecursiveComparison().isEqualTo(successClient);
    }

    @Test
    void takeLoan_whenAmountExceeded() {
        // given
        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));

        // when
        assertThatThrownBy(() -> underTest.takeLoan(failedClient, httpServletRequest))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("The amount you are trying to borrow exceeds the max amount!");
    }

    @Test
    void postponeLoan_whenLoanExistAndFirstPostpone() {
        // given
        when(config.getPostponeDays()).thenReturn(7);
        when(config.getInterestIncrementFactor()).thenReturn(1.5);
        when(loanRepository.findById(anyInt())).thenReturn(Optional.of(successfulLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loanWithPostpone);

        // when
        LoanPostpone actualLoanPostpone = underTest.postponeLoan(1);

        // then
        assertThat(actualLoanPostpone).usingRecursiveComparison().isEqualTo(firstLoanPostpone);
    }

    @Test
    void postponeLoan_whenLoanExistAndNotFirstPostpone() {
        // given
        when(config.getPostponeDays()).thenReturn(7);
        when(config.getInterestIncrementFactor()).thenReturn(1.5);
        when(loanRepository.findById(anyInt())).thenReturn(Optional.of(loanWithPostpone));
        when(loanRepository.save(any(Loan.class))).thenReturn(loanWithTwoPostpones);

        // when
        LoanPostpone actualLoanPostpone = underTest.postponeLoan(1);

        // then
        assertThat(actualLoanPostpone).usingRecursiveComparison().isEqualTo(secondLoanPostpone);
    }

    @Test
    void postponeLoan_whenLoanNotExist() {
        // given
        when(loanRepository.findById(anyInt())).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> underTest.postponeLoan(1))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Loan with id 1 does not exist.");

    }

    @Test
    void getClientHistory_whenClientExist() {
        // given
        when(clientRepository.findById(anyString())).thenReturn(Optional.of(clientFromRepo));

        // when
        Collection<Loan> clientHistory = underTest.getClientHistory("EXISTING-ID");

        // then
        assertThat(clientHistory).usingRecursiveComparison().isEqualTo(clientFromRepo.getLoans());
    }

    @Test
    void getClientHistory_whenClientNotExist() {
        // given
        when(clientRepository.findById(anyString())).thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> underTest.getClientHistory("NON-EXISTING-ID"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Client with id NON-EXISTING-ID does not exist.");
    }
}