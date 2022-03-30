package io.osvaldas.loans.domain.util;

import static java.time.ZoneId.of;
import static java.time.ZonedDateTime.now;

import java.time.ZonedDateTime;

import org.springframework.stereotype.Component;

@Component
public class TimeService implements TimeUtils {

    static final String TIME_ZONE = "Europe/Vilnius";

    @Override
    public int getHourOfDay() {
        return getCurrentDateTime().getHour();
    }

    @Override
    public ZonedDateTime getCurrentDateTime() {
        return now(of(TIME_ZONE));
    }
}