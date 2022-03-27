package io.osvaldas.loans.domain.util

import static io.osvaldas.loans.domain.util.TimeService.TIME_ZONE
import static java.time.ZoneId.of
import static java.time.ZonedDateTime.now

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
            with(currentDateTime) {
                year == now.year
                month == now.month
                dayOfMonth == now.dayOfMonth
                hour == now.hour
                minute == now.minute
            }
    }

    void 'should return current hour of the day'() {
        when:
            int currentHour = service.hourOfDay
        then:
            currentHour == now.hour
    }
}
