package io.osvaldas.loans.domain.clients

import static java.lang.String.format
import static java.util.Optional.empty
import static java.util.Optional.of

import io.osvaldas.loans.domain.exceptions.BadRequestException
import io.osvaldas.loans.domain.exceptions.NotFoundException
import io.osvaldas.loans.repositories.ClientRepository
import io.osvaldas.loans.repositories.entities.Client
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class ClientServiceSpec extends Specification {

    public static final long existingPersonalCode = 12345678910

//    @Shared
//    String timeZone = 'Europe/Vilnius'
//
//    @Shared
//    ZonedDateTime date = generateDate()
//
    @Shared
    String clientId = 'userId'

    @Shared
    String errorMessage = 'Client with id %s does not exist.'
//
//    @Shared
//    LoanPostpone firstPostpone = buildExtension(15.00, date.plusWeeks(1))
//
//    @Shared
//    LoanPostpone secondPostpone = buildExtension(22.50, date.plusWeeks(2))
//
//    @Shared
//    Loan successfulLoan = buildLoan(100.00)
//
//    @Shared
//    Loan loanWithPostpone = buildLoanWithPostpone(buildLoan(100.00), firstPostpone)

    @Shared
    Client clientWithoutId = buildClient('')

    Client clientWithId = buildClient(clientId)

    ClientRepository clientRepository = Mock()

    @Subject
    ClientService clientService = new ClientService(clientRepository, errorMessage)

    void 'should throw exception when registering client with existing personal code'() {
        when:
            clientService.registerClient(clientWithoutId)
        then:
            BadRequestException e = thrown()
            e.message == 'Client with personal code already exists.'
        and:
            1 * clientRepository.existsByPersonalCode(clientWithoutId.personalCode) >> true
    }

    void 'should register new client when client with new personal code'() {
        when:
            Client registeredClient = clientService.registerClient(clientWithoutId)
        then:
            registeredClient.id == clientId
        and:
            1 * clientRepository.existsByPersonalCode(clientWithoutId.personalCode) >> false
            1 * clientRepository.save(clientWithoutId) >> clientWithId
    }

    void 'should return clients list when there are clients'() {
        when:
            Collection clients = clientService.getClients()
        then:
            clients.size() == 2
        and:
            clients == [clientWithId, clientWithId]
        and:
            1 * clientRepository.findAll() >> [clientWithId, clientWithId]
    }

    void 'should return empty list when there are no clients'() {
        when:
            Collection clients = clientService.getClients()
        then:
            clients == []
        and:
            1 * clientRepository.findAll() >> []
    }

    void 'should return client when it exists'() {
        when:
            Client client = clientService.getClient(clientId)
        then:
            client.id == clientId
        and:
            1 * clientRepository.findById(clientId) >> of(clientWithId)
    }

    void 'should throw exception when client does not exist'() {
        when:
            clientService.getClient(clientId)
        then:
            NotFoundException e = thrown()
            e.message == format(errorMessage, clientId)
        and:
            1 * clientRepository.findById(clientId) >> empty()
    }

//    private Loan buildLoan(BigDecimal loanAmount) {
//        new Loan().tap {
//            id = 1
//            amount = loanAmount
//            termInMonths = 12
//            interestRate = 10
//            returnDate = date.plusYears(1)
//        }
//    }
//
//    private LoanPostpone buildExtension(BigDecimal newRate, ZonedDateTime newDate) {
//        new LoanPostpone().tap {
//            id = 1
//            newInterestRate = newRate
//            newReturnDate = newDate
//        }
//    }
//
//    private ZonedDateTime generateDate() {
//        return ZonedDateTime.of(
//            2020,
//            1,
//            1,
//            1,
//            1,
//            1,
//            1,
//            ZoneId.of(timeZone))
//    }
//
//    private Loan buildLoanWithPostpone(Loan loanRequest, LoanPostpone postpone) {
//        List<LoanPostpone> list = singletonList(postpone)
//        loanRequest.with { loanPostpones = new HashSet<>(list) }
//        return loanRequest
//    }
//
//    private Loan buildLoanWithPostpones(Loan loanRequest, LoanPostpone postpone, LoanPostpone secondPostpone) {
//        List<LoanPostpone> list = Arrays.asList(postpone, secondPostpone)
//        loanRequest.with { loanPostpones = new HashSet<>(list) }
//        return loanRequest
//    }

    private Client buildClient(String clientId) {
        return new Client().tap {
            id = clientId
            firstName = 'Testas'
            lastName = 'Testaitis'
            personalCode = existingPersonalCode
        }
    }
}