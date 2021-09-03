package com.finance.loans.domain.util;

import java.time.ZonedDateTime;

public interface TimeUtils {

    ZonedDateTime getCurrentDateTime();

    int getHourOfDay();
}
