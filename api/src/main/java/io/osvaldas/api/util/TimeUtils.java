package io.osvaldas.api.util;

import java.time.ZonedDateTime;

public interface TimeUtils {

    ZonedDateTime getCurrentDateTime();

    int getHourOfDay();
}
