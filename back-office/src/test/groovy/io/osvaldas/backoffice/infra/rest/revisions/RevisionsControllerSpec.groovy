package io.osvaldas.backoffice.infra.rest.revisions

import static io.osvaldas.api.clients.Status.ACTIVE
import static io.osvaldas.api.clients.Status.INACTIVE
import static io.osvaldas.api.loans.Status.CLOSED
import static io.osvaldas.api.loans.Status.OPEN
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

import org.springframework.mock.web.MockHttpServletResponse

import io.osvaldas.api.clients.ClientResponse
import io.osvaldas.api.loans.LoanResponse
import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.Loan
import spock.lang.Shared

class RevisionsControllerSpec extends AbstractControllerSpec {

    @Shared
    String id = 'revisionClientId'

    void 'should return client revisions when they exist'() {
        given:
            Client savedClient = clientRepository.save(buildClient(id, [loan] as Set, ACTIVE))
            savedClient.status = INACTIVE
            clientRepository.save(savedClient)
        when:
            MockHttpServletResponse response = mockMvc
                .perform(get('/api/v1/revisions/clients/{clientId}', savedClient.id)
                    .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            (objectMapper.readValue(response.contentAsString, ClientResponse[]) as List)*.status() == [ACTIVE, INACTIVE]
    }

    void 'should return loan revisions when they exist'() {
        given:
            Loan savedLoan = loanRepository.save(buildLoan(10.0, OPEN))
            savedLoan.status = CLOSED
            loanRepository.save(savedLoan)
        when:
            MockHttpServletResponse response = mockMvc
                .perform(get('/api/v1/revisions/loans/{loanId}', savedLoan.id)
                    .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            (objectMapper.readValue(response.contentAsString, LoanResponse[]) as List)*.status() == [OPEN, CLOSED]
    }

}
