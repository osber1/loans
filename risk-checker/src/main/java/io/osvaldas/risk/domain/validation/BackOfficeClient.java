package io.osvaldas.risk.domain.validation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.api.loans.TodayTakenLoansCount;

@FeignClient(name = "back-office", url = "${back-office.url:}")
public interface BackOfficeClient {

    @GetMapping("api/v1/loans/{loanId}")
    LoanResponse getLoan(@PathVariable long loanId);

    @GetMapping("api/v1/loans/today")
    TodayTakenLoansCount getLoansTakenTodayCount(@RequestParam String clientId);

}
