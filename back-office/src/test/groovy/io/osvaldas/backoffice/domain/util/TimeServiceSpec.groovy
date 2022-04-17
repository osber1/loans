package io.osvaldas.backoffice.domain.util

import static java.time.Clock.systemUTC
import static java.time.ZonedDateTime.now
import static java.time.temporal.ChronoUnit.SECONDS

import java.time.Clock
import java.time.ZonedDateTime

import io.osvaldas.api.util.TimeService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class TimeServiceSpec extends Specification {

    @Shared
    Clock clock = systemUTC()

    @Shared
    ZonedDateTime now = now(clock)

    @Subject
    TimeService service = new TimeService(clock)

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
