package io.osvaldas.loans.infra.rest.extensions;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.loans.domain.extensions.ExtensionService;
import io.osvaldas.loans.infra.rest.extensions.dtos.LoanExtensionResponse;
import io.osvaldas.loans.repositories.mapper.LoanExtensionMapper;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class ExtensionsController {

    private final ExtensionService service;

    private final LoanExtensionMapper postponeMapper;

    @PostMapping("client/loans/{loanId}/extensions")
    public LoanExtensionResponse postponeLoan(@PathVariable long loanId) {
        return postponeMapper.loanPostponeToDTO(service.postponeLoan(loanId));
    }
}
