package com.finance.interest.interfaces;

import java.time.ZonedDateTime;

public interface TimeUtils {

    ZonedDateTime getCurrentDateTime();

    int getHourOfDay();

    ZonedDateTime getDayOfMonth();
}
