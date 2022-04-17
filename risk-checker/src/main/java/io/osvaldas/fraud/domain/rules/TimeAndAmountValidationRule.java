package io.osvaldas.fraud.domain.rules;

import java.math.BigDecimal;

public interface TimeAndAmountValidationRule {

    void validate(BigDecimal amount);
}
