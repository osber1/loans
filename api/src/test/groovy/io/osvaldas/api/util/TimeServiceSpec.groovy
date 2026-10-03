package io.osvaldas.api.util

import static java.time.Clock.fixed
import static java.time.Instant.parse
import static java.time.ZoneId.of

import java.time.ZonedDateTime

import spock.lang.Specification
import spock.lang.Subject

class TimeServiceSpec extends Specification {

    @Subject
    TimeService timeService = new TimeService(fixed(parse('2021-10-12T10:10:10Z'), of('UTC')))

    void 'should return current date time from the clock'() {
        expect:
            timeService.currentDateTime == ZonedDateTime.parse('2021-10-12T10:10:10Z[UTC]')
    }

    void 'should return current hour of day from the clock'() {
        expect:
            timeService.hourOfDay == 10
    }

}
