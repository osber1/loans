package com.finance.loans.domain.loans.rules;

import java.math.BigDecimal;

public interface Validator {

    void validate(BigDecimal clientAmount);
}