package org.skysongdev.skysongitems.timer;

import java.time.Clock;
import java.util.Optional;

/** Real time, in milliseconds since 1970 (UTC), stepped by {@link TimeSpan}s such as {@code 24h} or {@code 1mo}. */
public final class RealTimeClock implements TimerClock {
    /** The id timers store for real time. */
    public static final String ID = "real";

    private final Clock clock;

    /** @param clock tells the time; tests pass a fixed one */
    public RealTimeClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public long now() {
        return clock.millis();
    }

    @Override
    public Optional<TimeSpan> parseStep(String text) {
        return TimeSpan.parse(text);
    }

    @Override
    public String describeWait(long from, long to) {
        long seconds = (to - from) / 1000;
        return seconds > 0 ? new TimeSpan(0, seconds).toString() : "";
    }
}
