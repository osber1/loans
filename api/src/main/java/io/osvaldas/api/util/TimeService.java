package io.osvaldas.api.util;

import static java.time.ZonedDateTime.now;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimeService implements TimeUtils {

    private final Clock clock;

    @Override
    public ZonedDateTime getCurrentDateTime() {
        return now(clock);
    }
}
