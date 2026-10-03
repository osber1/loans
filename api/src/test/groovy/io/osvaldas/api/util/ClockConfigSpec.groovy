package io.osvaldas.api.util

import static java.time.Clock.fixed
import static java.time.Instant.parse
import static java.time.ZoneId.of
import static java.time.temporal.ChronoUnit.DAYS

import java.time.Clock
import java.time.ZoneId
import java.time.ZonedDateTime

import spock.lang.Specification

class ClockConfigSpec extends Specification {

    ClockConfig config = new ClockConfig()

    void 'should create clock in the configured time zone'() {
        expect:
            config.clock(of('Europe/Vilnius')).zone == of('Europe/Vilnius')
    }

    void 'should use the business zone for hour of day and start of day'() {
        given: 'late evening UTC, which is already after midnight in Vilnius (EEST, UTC+3)'
            Clock clock = fixed(parse('2022-10-12T22:30:00Z'), ZoneId.of('Europe/Vilnius'))
            TimeService timeService = new TimeService(clock)
        expect:
            timeService.hourOfDay == 1
            timeService.currentDateTime.truncatedTo(DAYS) ==
                ZonedDateTime.parse('2022-10-13T00:00:00+03:00[Europe/Vilnius]')
    }

    void 'should follow daylight saving time changes'() {
        given: 'same UTC hour, before and after the end of summer time in Vilnius'
            TimeService summer = new TimeService(fixed(parse('2022-10-29T21:30:00Z'), of('Europe/Vilnius')))
            TimeService winter = new TimeService(fixed(parse('2022-10-30T21:30:00Z'), of('Europe/Vilnius')))
        expect:
            summer.hourOfDay == 0
            winter.hourOfDay == 23
    }

}
