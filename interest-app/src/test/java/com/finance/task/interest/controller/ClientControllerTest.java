package com.finance.task.interest.controller;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.interest.ClientRequest;
import com.finance.interest.ClientResponse;
import com.finance.interest.LoanRequest;
import com.finance.interest.LoanResponse;
import com.finance.interest.model.ClientDAO;
import com.finance.interest.model.IpLogs;
import com.finance.interest.model.Loan;
import com.finance.interest.model.LoanPostpone;
import com.finance.interest.repository.ClientRepository;
import com.finance.interest.repository.IpLogsRepository;
import com.finance.interest.repository.LoanRepository;
import com.finance.interest.util.TimeUtils;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.yaml")
class ClientControllerTest {

    public static final String TIME_ZONE = "Europe/Vilnius";

    public static final ZonedDateTime DATE_TIME_OF_JAN_1_2021 = ZonedDateTime.of(2021, 1, 1, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

    public static final ZonedDateTime DATE_TIME_OF_JAN_8_2021 = ZonedDateTime.of(2021, 1, 8, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

    public static final ZonedDateTime DATE_OF_JAN_1_2021 = ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of(TIME_ZONE));

    public static final String NAME = "Name";

    public static final String SURNAME = "Surname";

    public static final String PERSONAL_CODE = "12345678910";

    public static final String ID = "TEST-ID-TEST";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private IpLogsRepository ipLogsRepository;

    @Autowired
    private LoanRepository loanRepository;

    private Loan loan;

    private ClientResponse clientDto;

    private ClientDAO clientDao;

    private LoanPostpone loanPostpone;

    @BeforeEach
    void setUp() {
        createLoanPostpone();
        createClientDto();
        createLoan();
        createClientDao();
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
        ipLogsRepository.deleteAll();
        loanRepository.deleteAll();
    }

    private void createLoanPostpone() {
        loanPostpone = new LoanPostpone();
        loanPostpone.setId(1);
        loanPostpone.setNewReturnDate(DATE_TIME_OF_JAN_8_2021);
        loanPostpone.setNewInterestRate(15.0);
    }

    private void createClientDao() {
        clientDao = new ClientDAO();
        clientDao.setId(ID);
        clientDao.setFirstName(NAME);
        clientDao.setLastName(SURNAME);
        clientDao.setPersonalCode(Long.parseLong(PERSONAL_CODE));
        clientDao.setLoans(new HashSet<>(Collections.singletonList(loan)));
    }

    private void createLoan() {
        loan = new Loan();
        loan.setAmount(BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_EVEN));
        loan.setInterestRate(10.0);
        loan.setTermInMonths(12);
        loan.setReturnDate(DATE_TIME_OF_JAN_1_2021);
    }

    private void createClientDto() {
        LoanResponse loan = new LoanResponse();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setInterestRate(10);
        loan.setTermInMonths(12);
        loan.setReturnDate(DATE_TIME_OF_JAN_1_2021);
        clientDto = new ClientResponse();
        clientDto.setFirstName(NAME);
        clientDto.setLastName(SURNAME);
        clientDto.setPersonalCode(PERSONAL_CODE);
        clientDto.setLoan(loan);
    }

    @Test
    void testTakeLoan_whenSuccessful() throws Exception {
        // given
        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clientDto))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isOk());
        List<ClientDAO> clients = clientRepository.findAll();
        assertThat(clients).usingElementComparatorIgnoringFields("id", "createdAt", "updatedAt", "loans").contains(clientDao);
    }

    @Test
    void testTakeLoan_FirstNameIsNull() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setTermInMonths(12);
        ClientRequest clientWithoutFirstName = new ClientRequest();
        clientWithoutFirstName.setLastName(SURNAME);
        clientWithoutFirstName.setPersonalCode(PERSONAL_CODE);
        clientWithoutFirstName.setLoan(loan);

        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clientWithoutFirstName))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("First name must be not empty.");
    }

    @Test
    void testTakeLoan_LastNameIsNull() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setTermInMonths(12);
        ClientRequest clientWithoutLastName = new ClientRequest();
        clientWithoutLastName.setFirstName(NAME);
        clientWithoutLastName.setPersonalCode(PERSONAL_CODE);
        clientWithoutLastName.setLoan(loan);

        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clientWithoutLastName))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("Last name must be not empty.");
    }

    @Test
    void testTakeLoan_whenLoanIsNull() throws Exception {
        // given
        ClientRequest clientWithoutLoan = new ClientRequest();
        clientWithoutLoan.setFirstName(NAME);
        clientWithoutLoan.setLastName(SURNAME);
        clientWithoutLoan.setPersonalCode(PERSONAL_CODE);

        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clientWithoutLoan))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("Loan must be not empty.");
    }

    @Test
    void testTakeLoan_whenPersonalCodeIsNull() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setTermInMonths(12);
        ClientRequest clientWithoutPersonalCode = new ClientRequest();
        clientWithoutPersonalCode.setFirstName(NAME);
        clientWithoutPersonalCode.setLastName(SURNAME);
        clientWithoutPersonalCode.setLoan(loan);

        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clientWithoutPersonalCode))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("Personal code must be not empty.");
    }

    @Test
    void testTakeLoan_whenPersonalCodeTooShort() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setTermInMonths(12);
        ClientRequest clientWithTooShortPersonalCode = new ClientRequest();
        clientWithTooShortPersonalCode.setFirstName(NAME);
        clientWithTooShortPersonalCode.setLastName(SURNAME);
        clientWithTooShortPersonalCode.setPersonalCode("123456789");
        clientWithTooShortPersonalCode.setLoan(loan);

        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clientWithTooShortPersonalCode))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("Personal code must be 11 digits length.");
    }

    @Test
    void testTakeLoan_whenPersonalCodeContainsLetters() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setTermInMonths(12);
        ClientRequest clientWithLettersInPersonalCode = new ClientRequest();
        clientWithLettersInPersonalCode.setFirstName(NAME);
        clientWithLettersInPersonalCode.setLastName(SURNAME);
        clientWithLettersInPersonalCode.setPersonalCode("123456789ab");
        clientWithLettersInPersonalCode.setLoan(loan);

        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clientWithLettersInPersonalCode))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("All characters must be digits.");
    }

    @Test
    void testTakeLoan_whenIpLimitExceedsSameDay() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setTermInMonths(12);
        ClientRequest client = new ClientRequest();
        client.setFirstName(NAME);
        client.setLastName(SURNAME);
        client.setPersonalCode("12345678910");
        client.setLoan(loan);

        IpLogs ipLogs = IpLogs.builder()
            .ip("0.0.0.0.0")
            .timesUsed(3)
            .firstRequestDate(DATE_TIME_OF_JAN_1_2021).build();
        ipLogsRepository.save(ipLogs);

        ResultActions resultActions;
        try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
            utilities.when(TimeUtils::getHourOfDay).thenReturn(7);
            utilities.when(TimeUtils::getCurrentDateTime).thenReturn(DATE_TIME_OF_JAN_1_2021);
            utilities.when(TimeUtils::getDayOfMonth).thenReturn(DATE_OF_JAN_1_2021);
            resultActions = mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(client))
                .with(request -> {
                    request.setRemoteAddr("0.0.0.0.0");
                    return request;
                }));
        }

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("Too many requests from the same ip per day.");
    }

    @Test
    void testTakeLoan_whenIpLimitExceedsNextDay() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setTermInMonths(12);
        ClientRequest client = new ClientRequest();
        client.setFirstName(NAME);
        client.setLastName(SURNAME);
        client.setPersonalCode("12345678910");
        client.setLoan(loan);

        IpLogs ipLogs = IpLogs.builder()
            .ip("0.0.0.0.0")
            .timesUsed(3)
            .firstRequestDate(DATE_TIME_OF_JAN_1_2021).build();
        ipLogsRepository.save(ipLogs);

        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(client))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isOk());
        List<ClientDAO> clients = clientRepository.findAll();
        assertThat(clients).usingElementComparatorIgnoringFields("id", "createdAt", "updatedAt", "loans").contains(clientDao);
    }

    @Test
    void testTakeLoan_whenAmountExceeds() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(500));
        loan.setTermInMonths(12);
        ClientRequest clientWithAmountExceeded = new ClientRequest();
        clientWithAmountExceeded.setFirstName(NAME);
        clientWithAmountExceeded.setLastName(SURNAME);
        clientWithAmountExceeded.setPersonalCode("12345678910");
        clientWithAmountExceeded.setLoan(loan);

        ResultActions resultActions = mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(clientWithAmountExceeded))
            .with(request -> {
                request.setRemoteAddr("0.0.0.0.0");
                return request;
            }));

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("The amount you are trying to borrow exceeds the max amount!");
    }

    @Test
    void testTakeLoan_whenMaxAmountAndForbiddenTime() throws Exception {
        // given
        LoanRequest loan = new LoanRequest();
        loan.setAmount(BigDecimal.valueOf(100));
        loan.setTermInMonths(12);
        ClientRequest clientWithMaxAmount = new ClientRequest();
        clientWithMaxAmount.setFirstName(NAME);
        clientWithMaxAmount.setLastName(SURNAME);
        clientWithMaxAmount.setPersonalCode("12345678910");
        clientWithMaxAmount.setLoan(loan);

        ResultActions resultActions;
        try (MockedStatic<TimeUtils> utilities = Mockito.mockStatic(TimeUtils.class)) {
            utilities.when(TimeUtils::getHourOfDay).thenReturn(2);
            resultActions = mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clientWithMaxAmount))
                .with(request -> {
                    request.setRemoteAddr("0.0.0.0.0");
                    return request;
                }));
        }

        // then
        resultActions.andExpect(status().isBadRequest());
        String contentAsString = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(contentAsString).contains("Risk is too high, because you are trying to get loan between 00:00 and 6:00 and you want to borrow the max amount!");
    }

    @Test
    void testPostponeLoan_whenSuccessful() throws Exception {
        // given
        ClientDAO savedClient = clientRepository.save(clientDao);
        MvcResult postponeResult = mockMvc.perform(post("/api/loans/{id}/postpone", savedClient.getLoans().stream().findFirst().get().getId())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        // then
        String contentAsString = postponeResult.getResponse().getContentAsString();
        LoanPostpone postpone = objectMapper.readValue(contentAsString, LoanPostpone.class);
        assertThat(postpone).usingRecursiveComparison().ignoringFields("newReturnDate").isEqualTo(loanPostpone);
    }

    @Test
    void testPostponeLoan_whenClientNotExist() throws Exception {
        // given
        MvcResult postponeResult = mockMvc.perform(post("/api/loans/{id}/postpone", 1)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn();

        // then
        String contentAsString = postponeResult.getResponse().getContentAsString();
        assertThat(contentAsString).contains("Loan with id 1 does not exist.");
    }

    @Test
    void testGetClientHistory_whenSuccessful() throws Exception {
        // given
        clientRepository.save(clientDao);
        MvcResult historyResult = mockMvc.perform(get("/api/clients/{id}/loans", ID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        // then
        String contentAsString = historyResult.getResponse().getContentAsString();
        List<Loan> loans = objectMapper.readValue(contentAsString, new TypeReference<>() {
        });
        assertThat(loans).usingElementComparatorIgnoringFields("id", "returnDate", "loanPostpones").contains(loan);
    }

    @Test
    void testGetClientHistory_whenClientNotExist() throws Exception {
        // given
        MvcResult historyResult = mockMvc.perform(get("/api/clients/{id}/loans", ID)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn();

        // then
        String contentAsString = historyResult.getResponse().getContentAsString();
        assertThat(contentAsString).contains("Client with id TEST-ID-TEST does not exist.");
    }

}