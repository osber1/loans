package com.finance.interest.interfaces;

import java.math.BigDecimal;

public interface Validator {

    void validate(String ip, BigDecimal clientAmount);
}
