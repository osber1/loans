package io.osvaldas.risk.domain.validation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.osvaldas.api.loans.LoanResponse;

@FeignClient(name = "back-office", url = "${back-office.url}")
public interface BackOfficeClient {

    @GetMapping("api/v1/loans/{loanId}")
    LoanResponse getLoan(@PathVariable long loanId);

}
