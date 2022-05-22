package io.osvaldas.risk;

import static java.util.Optional.ofNullable;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class TestClockDelegate extends Clock {

    private final Clock defaultDelegate;

    private Clock delegate;

    public TestClockDelegate(Clock defaultDelegate) {
        this.defaultDelegate = defaultDelegate;
    }

    @Override
    public ZoneId getZone() {
        return currentDelegate().getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return currentDelegate().withZone(zone);
    }

    @Override
    public Instant instant() {
        return currentDelegate().instant();
    }

    public void changeDelegate(Clock delegate) {
        this.delegate = delegate;
    }

    public void reset() {
        delegate = null;
    }

    Clock currentDelegate() {
        return ofNullable(delegate).orElse(defaultDelegate);
    }

}
