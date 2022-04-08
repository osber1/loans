package io.osvaldas.backoffice.domain.util;

import java.time.ZonedDateTime;

public interface TimeUtils {

    ZonedDateTime getCurrentDateTime();

    int getHourOfDay();
}
