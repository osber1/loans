package io.osvaldas.backoffice.infra.rest.postpones

import static io.osvaldas.api.clients.Status.REGISTERED
import static java.lang.String.format
import static java.util.Set.of
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.mock.web.MockHttpServletResponse

import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.LoanPostpone

class PostponeControllerSpec extends AbstractControllerSpec {

    void 'should postpone loan when it exists'() {
        given:
            Client savedClient = clientRepository.save(buildClient(clientId, of(loan), REGISTERED))
        when:
            MockHttpServletResponse response = mockMvc
                .perform(post('/api/v1/loans/extensions')
                    .param('loanId', "${savedClient.loans[0].id}")
                    .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            LoanPostpone postpone = objectMapper.readValue(response.contentAsString, LoanPostpone)
            with(postpone) {
                id == firstPostpone.id
                interestRate == firstPostpone.interestRate
            }
    }

    void 'should throw an exception when trying to postpone non existing loan'() {
        given:
            int nonExistingId = 5555
        when:
            MockHttpServletResponse response = mockMvc
                .perform(post('/api/v1/loans/extensions')
                    .param('loanId', "${nonExistingId}")
                    .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == NOT_FOUND.value()
        and:
            response.contentAsString.contains(format(loanNotFound, nonExistingId))
    }

}
