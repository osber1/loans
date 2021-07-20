package com.finance.interest.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    public static final String TIME_ZONE = "Europe/Vilnius";

    public static ZonedDateTime getDayOfMonth() {
        return getCurrentDateTime().truncatedTo(ChronoUnit.DAYS);
    }

    public static int getHourOfDay() {
        return getCurrentDateTime().getHour();
    }

    public static ZonedDateTime getCurrentDateTime() {
        return ZonedDateTime.now(ZoneId.of(TIME_ZONE));
    }
}
