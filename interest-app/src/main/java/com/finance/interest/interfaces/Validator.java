package com.finance.interest.interfaces;

import java.math.BigDecimal;

public interface Validator {

    void validate(BigDecimal clientAmount);
}