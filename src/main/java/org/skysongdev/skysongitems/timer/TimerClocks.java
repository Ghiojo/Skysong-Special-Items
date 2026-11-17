package org.skysongdev.skysongitems.timer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** The clocks timers can count in, by id. Real time is always there; other plugins add theirs. */
public final class TimerClocks {
    private final Map<String, TimerClock> byId = new HashMap<>();
    private final TimerClock realTime;

    public TimerClocks(TimerClock realTime) {
        byId.put(normalise(realTime.id()), realTime);
        this.realTime = realTime;
    }

    /**
     * Adds a clock, or replaces the one another plugin registered under the same id before,
     * such as after {@code /si reload}.
     *
     * @throws IllegalArgumentException when the id is that of real time
     */
    public void register(TimerClock clock) {
        String id = normalise(clock.id());
        if (realTime != null && id.equals(normalise(realTime.id()))) {
            throw new IllegalArgumentException("The clock id " + clock.id() + " belongs to real time");
        }
        byId.put(id, clock);
    }

    /** The clock with this id, ignoring case; empty when no plugin registered it (yet). */
    public Optional<TimerClock> find(String id) {
        return Optional.ofNullable(byId.get(normalise(id)));
    }

    /** Real time. */
    public TimerClock realTime() {
        return realTime;
    }

    private static String normalise(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
