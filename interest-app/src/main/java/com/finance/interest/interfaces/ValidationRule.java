package com.finance.interest.interfaces;

import java.math.BigDecimal;

public interface ValidationRule {

    void validate(String ip, BigDecimal clientAmount);
}
