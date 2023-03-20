package io.osvaldas.backoffice.infra.rest.postpones;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.osvaldas.api.postpones.LoanPostponeResponse;
import io.osvaldas.backoffice.domain.postpones.PostponeService;
import io.osvaldas.backoffice.repositories.mapper.LoanPostponesMapper;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1")
public class PostponeController {

    private final PostponeService service;

    private final LoanPostponesMapper postponeMapper;

    @PostMapping("loans/extensions")
    public LoanPostponeResponse postponeLoan(@RequestParam long loanId) {
        return postponeMapper.map(service.postponeLoan(loanId));
    }
}
