package io.osvaldas.backoffice.infra.rest.postpones

import static java.lang.String.format
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.MediaType.APPLICATION_JSON
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post

import org.springframework.mock.web.MockHttpServletResponse

import io.osvaldas.backoffice.infra.rest.AbstractControllerSpec
import io.osvaldas.backoffice.repositories.entities.Client
import io.osvaldas.backoffice.repositories.entities.LoanPostpone

class PostponeControllerSpec extends AbstractControllerSpec {

    @SuppressWarnings('LineLength')
    void 'should postpone loan when it exists'() {
        given:
            Client savedClient = clientRepository.save(registeredClientWithLoan)
        when:
            MockHttpServletResponse response = mockMvc.perform(post('/api/v1/client/loans/{id}/extensions', savedClient.loans[0].id)
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

    @SuppressWarnings('LineLength')
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
