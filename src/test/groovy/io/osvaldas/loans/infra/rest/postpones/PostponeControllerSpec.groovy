package io.osvaldas.loans.infra.rest.postpones

import static java.lang.String.format
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.mock.web.MockHttpServletResponse

import io.osvaldas.loans.infra.rest.AbstractControllerSpec
import io.osvaldas.loans.repositories.entities.Client
import io.osvaldas.loans.repositories.entities.LoanPostpone

class PostponeControllerSpec extends AbstractControllerSpec {

    void 'should postpone loan when it exists'() {
        given:
            Client savedClient = clientRepository.save(clientWithLoan)
        when:
            MockHttpServletResponse response = mockMvc.perform(post('/api/v1/client/loans/{id}/extensions', savedClient.loans[0].id)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == OK.value()
        and:
            LoanPostpone postpone = objectMapper.readValue(response.contentAsString, LoanPostpone.class)
            with(postpone) {
                id == firstPostpone.id
                interestRate == firstPostpone.interestRate
            }
    }

    void 'should throw an exception when trying to postpone non existing loan'() {
        given:
            int nonExistingId = 5555
        when:
            MockHttpServletResponse response = mockMvc.perform(post('/api/v1/client/loans/{id}/extensions', nonExistingId)
                .contentType(APPLICATION_JSON))
                .andReturn().response
        then:
            response.status == NOT_FOUND.value()
        and:
            response.contentAsString.contains(format(loanErrorMessage, nonExistingId))
    }
}
