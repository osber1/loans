package io.osvaldas.loans.domain.util;

import java.time.ZonedDateTime;

public interface TimeUtils {

    ZonedDateTime getCurrentDateTime();

    int getHourOfDay();
}
