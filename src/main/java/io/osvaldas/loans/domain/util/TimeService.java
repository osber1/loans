package io.osvaldas.loans.domain.util;

import static java.time.ZonedDateTime.now;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

@Component
public class TimeService implements TimeUtils {

    private static final String TIME_ZONE = "Europe/Vilnius";

    @Override
    public int getHourOfDay() {
        return getCurrentDateTime().getHour();
    }

    @Override
    public ZonedDateTime getCurrentDateTime() {
        return now(ZoneId.of(TIME_ZONE));
    }
}