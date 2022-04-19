package io.osvaldas.fraud.domain.validation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.osvaldas.api.loans.LoanResponse;
import io.osvaldas.api.loans.TodayTakenLoansCount;

@FeignClient(name = "back-office", url = "http://localhost:8080")
public interface BackOfficeClient {

    @GetMapping("api/v1/loans/{loanId}")
    LoanResponse getLoan(@PathVariable long loanId);

    @GetMapping("api/v1/client/{clientId}/loans/today")
    TodayTakenLoansCount getLoansTakenTodayCount(@PathVariable String clientId);

}
