package io.osvaldas.backoffice.domain.util

import static TimeService.TIME_ZONE
import static java.time.ZoneId.of
import static java.time.ZonedDateTime.now
import static java.time.temporal.ChronoUnit.SECONDS

import java.time.ZonedDateTime

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class TimeServiceSpec extends Specification {

    @Shared
    ZonedDateTime now = now(of(TIME_ZONE))

    @Subject
    TimeService service = new TimeService()

    void 'should return current time'() {
        when:
            ZonedDateTime currentDateTime = service.currentDateTime
        then:
            currentDateTime.truncatedTo(SECONDS) == now.truncatedTo(SECONDS)
    }

    void 'should return current hour of the day'() {
        when:
            int currentHour = service.hourOfDay
        then:
            currentHour == now.hour
    }

}
