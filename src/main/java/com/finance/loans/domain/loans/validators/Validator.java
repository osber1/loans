package com.finance.loans.domain.loans.validators;

import java.math.BigDecimal;

public interface Validator {

    void validate(BigDecimal clientAmount);
}