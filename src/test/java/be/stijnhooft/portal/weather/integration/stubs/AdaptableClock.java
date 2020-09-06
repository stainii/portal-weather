package be.stijnhooft.portal.weather.integration.stubs;


import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AdaptableClock extends Clock {

    private final AtomicReference<Clock> wrappedClock;

    public AdaptableClock() {
        this(Instant.now());
    }

    public AdaptableClock(Instant instant) {
        this.wrappedClock = new AtomicReference<>(createFixedClock(instant));
    }

    public void changeClock(Instant instant) {
        this.wrappedClock.set(createFixedClock(instant));
    }

    private Clock createFixedClock(Instant instant) {
        return Clock.fixed(instant, ZoneId.systemDefault());
    }

    @Override
    public ZoneId getZone() {
        return wrappedClock.get().getZone();
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return wrappedClock.get().withZone(zone);
    }

    @Override
    public Instant instant() {
        return wrappedClock.get().instant();
    }

    public void tickBack(int timeAmount, ChronoUnit timeUnit) {
        changeClock(wrappedClock.get().instant().minus(timeAmount, timeUnit));
    }

    public void tickForward(int timeAmount, ChronoUnit timeUnit) {
        changeClock(wrappedClock.get().instant().plus(timeAmount, timeUnit));
    }
}

