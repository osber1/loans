package io.osvaldas.fraud.domain.rules;

public interface LoanLimitValidationRule {

    void validate(String clientId);
}
