package com.finance.interest.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.finance.interest.interfaces.TimeUtils;

@Component
public class TimeService implements TimeUtils {

    private static final String TIME_ZONE = "Europe/Vilnius";

    @Override
    public ZonedDateTime getDayOfMonth() {
        return getCurrentDateTime().truncatedTo(ChronoUnit.DAYS);
    }

    @Override
    public int getHourOfDay() {
        return getCurrentDateTime().getHour();
    }

    @Override
    public ZonedDateTime getCurrentDateTime() {
        return ZonedDateTime.now(ZoneId.of(TIME_ZONE));
    }
}