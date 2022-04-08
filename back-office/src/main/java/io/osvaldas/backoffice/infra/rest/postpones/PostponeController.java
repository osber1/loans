package io.osvaldas.backoffice.infra.rest.postpones;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.backoffice.domain.postpones.PostponeService;
import io.osvaldas.backoffice.infra.rest.postpones.dtos.LoanPostponeResponse;
import io.osvaldas.backoffice.repositories.mapper.LoanPostponesMapper;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class PostponeController {

    private final PostponeService service;

    private final LoanPostponesMapper postponeMapper;

    @PostMapping("client/loans/{loanId}/extensions")
    public LoanPostponeResponse postponeLoan(@PathVariable long loanId) {
        return postponeMapper.loanPostponeToDTO(service.postponeLoan(loanId));
    }
}
