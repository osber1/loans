//    public static final String TIME_ZONE = "Europe/Vilnius";

//    public static final ZonedDateTime DATE_TIME_OF_JAN_1_2021 = ZonedDateTime.of(2021, 1, 1, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

//    public static final ZonedDateTime DATE_TIME_OF_JAN_8_2021 = ZonedDateTime.of(2021, 1, 8, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

//    public static final ZonedDateTime DATE_TIME_OF_JAN_15_2021 = ZonedDateTime.of(2021, 1, 15, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

//    public static final ZonedDateTime DATE_OF_DEC_30_2020 = ZonedDateTime.of(2020, 12, 30, 0, 0, 0, 0, ZoneId.of(TIME_ZONE));

//    public static final ZonedDateTime DATE_OF_JAN_1_2021 = ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneId.of(TIME_ZONE));

//    public static final ZonedDateTime DATE_TIME_OF_JAN_1_2022 = ZonedDateTime.of(2022, 1, 1, 1, 1, 1, 1, ZoneId.of(TIME_ZONE));

//    private static Client successClient;

//    private static Client failedClient;

//    private static ClientDAO clientFromRepo;

//    private static LoanPostpone firstLoanPostpone;

//    private static LoanPostpone secondLoanPostpone;

//    private static Loan successfulLoan;

//    private static Loan loanWithPostpone;

//    private static Loan loanWithTwoPostpones;

//    private static IpLogs ipLog;

//    private static IpLogs ipLogAfter;

//    private static IpLogs ipLogWithLimit;

//    private static MockHttpServletRequest httpServletRequest;

//    @BeforeAll
//    static void init() {
//        successfulLoan = createLoan(DATE_TIME_OF_JAN_1_2022, BigDecimal.valueOf(100));
//        firstLoanPostpone = createLoanPostpone(DATE_TIME_OF_JAN_8_2021, 15);
//        secondLoanPostpone = createLoanPostpone(DATE_TIME_OF_JAN_15_2021, 22.5);
//        loanWithPostpone = createLoanWithPostpone(createLoan(DATE_TIME_OF_JAN_1_2022, BigDecimal.valueOf(100)), firstLoanPostpone);
//        loanWithTwoPostpones = createLoanWithPostpones(createLoan(DATE_TIME_OF_JAN_1_2022, BigDecimal.valueOf(100)), firstLoanPostpone, secondLoanPostpone);
//        successClient = createClient(successfulLoan);
//        failedClient = createClient(createLoan(null, BigDecimal.valueOf(1000)));
//        clientFromRepo = createClientDao(successfulLoan);
//        ipLog = createIpLog(DATE_TIME_OF_JAN_1_2021, 1);
//        ipLogWithLimit = createIpLog(DATE_TIME_OF_JAN_1_2021, 3);
//        ipLogAfter = createIpLog(DATE_OF_DEC_30_2020, 1);
//        httpServletRequest = createServletRequest();
//    }

//    private static LoanPostpone createLoanPostpone(ZonedDateTime date, double interestRate) {
//        LoanPostpone loanPostpone = new LoanPostpone();
//        loanPostpone.setNewReturnDate(date);
//        loanPostpone.setNewInterestRate(BigDecimal.valueOf(interestRate));
//        return loanPostpone;
//    }

//    private static MockHttpServletRequest createServletRequest() {
//        httpServletRequest = new MockHttpServletRequest();
//        httpServletRequest.setRemoteAddr("0.0.0.0.0");
//        return httpServletRequest;
//    }

//    private static IpLogs createIpLog(ZonedDateTime date, int timesUsed) {
//        return IpLogs.builder()
//            .id(1)
//            .ip("0.0.0.0.1")
//            .timesUsed(timesUsed)
//            .firstRequestDate(date)
//            .build();
//    }

//    private static ClientDAO createClientDao(Loan loanRequest) {
//        return ClientDAO.builder()
//            .id("ID-TO-TEST")
//            .firstName("Test")
//            .lastName("User")
//            .personalCode(11111111111L)
//            .loans(new HashSet<>(Collections.singletonList(loanRequest))).build();
//    }
//
//    private static Client createClient(Loan loanRequest) {
//        return Client.builder()
//            .id("ID-TO-TEST")
//            .firstName("Test")
//            .lastName("User")
//            .personalCode(11111111111L)
//            .loan(loanRequest).build();
//    }
//
//    private static Loan createLoan(ZonedDateTime date, BigDecimal amount) {
//        Loan successLoan = new Loan();
//        successLoan.setId(1);
//        successLoan.setInterestRate(BigDecimal.valueOf(10.0));
//        successLoan.setTermInMonths(12);
//        successLoan.setAmount(amount);
//        successLoan.setReturnDate(date);
//        return successLoan;
//    }
//
//    private static Loan createLoanWithPostpone(Loan loanRequest, LoanPostpone postpone1) {
//        List<LoanPostpone> list = Collections.singletonList(postpone1);
//        loanRequest.setLoanPostpones(new HashSet<>(list));
//        return loanRequest;
//    }
//
//    private static Loan createLoanWithPostpones(Loan loanRequest, LoanPostpone postpone1, LoanPostpone postpone2) {
//        List<LoanPostpone> list = Arrays.asList(postpone1, postpone2);
//        loanRequest.setLoanPostpones(new HashSet<>(list));
//        return loanRequest;
//    }
//
//    @Test
//    void takeLoan_whenNewUser() {
//        // given
//        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));
//        when(config.getInterestRate()).thenReturn(BigDecimal.valueOf(10.0));
//        when(config.getRequestsFromSameIpLimit()).thenReturn(3);
//        when(clientsIpsRepository.findByIp(anyString())).thenReturn(Optional.of(ipLog));
//        when(clientRepository.save(any(ClientDAO.class))).thenReturn(clientFromRepo);
//
//        // when
//        Client actualRequest;
//        try (MockedStatic<TimeUtilsImpl> utilities = Mockito.mockStatic(TimeUtilsImpl.class)) {
////            utilities.when(TimeUtilsImpl::getHourOfDay).thenReturn(7);
////            utilities.when(TimeUtilsImpl::getCurrentDateTime).thenReturn(DATE_TIME_OF_JAN_1_2021);
////            utilities.when(TimeUtilsImpl::getDayOfMonth).thenReturn(DATE_OF_JAN_1_2021);
//            actualRequest = underTest.takeLoan(successClient, httpServletRequest);
//        }
//
//        // then
//        assertThat(actualRequest).usingRecursiveComparison().isEqualTo(successClient);
//    }
//
//    @Test
//    void takeLoan_whenExistingUser() {
//        // given
//        when(config.getMaxAmount()).thenReturn(BigDecimal.valueOf(100));
//        when(config.getInterestRate()).thenReturn(BigDecimal.valueOf(10.0));
//        when(config.getRequestsFromSameIpLimit()).thenReturn(3);
//        when(clientsIpsRepository.findByIp(anyString())).thenReturn(Optional.of(ipLog));
//        when(clientRepository.save(any(ClientDAO.class))).thenReturn(clientFromRepo);
//        when(clientRepository.findByPersonalCode(anyLong())).thenReturn(Optional.of(clientFromRepo));
//
//        // when
//        Client actualRequest;
//        try (MockedStatic<TimeUtilsImpl> utilities = Mockito.mockStatic(TimeUtilsImpl.class)) {
////            utilities.when(TimeUtilsImpl::getHourOfDay).thenReturn(7);
////            utilities.when(TimeUtilsImpl::getCurrentDateTime).thenReturn(DATE_TIME_OF_JAN_1_2021);
////            utilities.when(TimeUtilsImpl::getDayOfMonth).thenReturn(DATE_OF_JAN_1_2021);
//            actualRequest = underTest.takeLoan(successClient, httpServletRequest);
//        }
//
//        // then
//        assertThat(actualRequest).usingRecursiveComparison().isEqualTo(successClient);
//    }
//    @Test
//    void postponeLoan_whenLoanExistAndFirstPostpone() {
//        // given
//        when(config.getPostponeDays()).thenReturn(7);
//        when(config.getInterestIncrementFactor()).thenReturn(BigDecimal.valueOf(1.5));
//        when(loanRepository.findById(anyInt())).thenReturn(Optional.of(successfulLoan));
//        when(loanRepository.save(any(Loan.class))).thenReturn(loanWithPostpone);
//
//        // when
//        LoanPostpone actualLoanPostpone = underTest.postponeLoan(1);
//
//        // then
//        assertThat(actualLoanPostpone).usingRecursiveComparison().isEqualTo(firstLoanPostpone);
//    }
//
//    @Test
//    void postponeLoan_whenLoanExistAndNotFirstPostpone() {
//        // given
//        when(config.getPostponeDays()).thenReturn(7);
//        when(config.getInterestIncrementFactor()).thenReturn(BigDecimal.valueOf(1.5));
//        when(loanRepository.findById(anyInt())).thenReturn(Optional.of(loanWithPostpone));
//        when(loanRepository.save(any(Loan.class))).thenReturn(loanWithTwoPostpones);
//
//        // when
//        LoanPostpone actualLoanPostpone = underTest.postponeLoan(1);
//
//        // then
//        assertThat(actualLoanPostpone).usingRecursiveComparison().isEqualTo(secondLoanPostpone);
//    }
//
//    @Test
//    void postponeLoan_whenLoanNotExist() {
//        // given
//        when(loanRepository.findById(anyInt())).thenReturn(Optional.empty());
//
//        // then
//        assertThatThrownBy(() -> underTest.postponeLoan(1))
//            .isInstanceOf(NotFoundException.class)
//            .hasMessageContaining("Loan with id 1 does not exist.");
//
//    }
//
//    @Test
//    void getClientHistory_whenClientExist() {
//        // given
//        when(clientRepository.findById(anyString())).thenReturn(Optional.of(clientFromRepo));
//
//        // when
//        Collection<Loan> clientHistory = underTest.getClientHistory("EXISTING-ID");
//
//        // then
//        assertThat(clientHistory).usingRecursiveComparison().isEqualTo(clientFromRepo.getLoans());
//    }
//
//    @Test
//    void getClientHistory_whenClientNotExist() {
//        // given
//        when(clientRepository.findById(anyString())).thenReturn(Optional.empty());
//
//        // then
//        assertThatThrownBy(() -> underTest.getClientHistory("NON-EXISTING-ID"))
//            .isInstanceOf(NotFoundException.class)
//            .hasMessageContaining("Client with id NON-EXISTING-ID does not exist.");
//    }
//}