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

    /**
     * Reads a time as staff type it, such as {@code 24h}, in the first clock that understands
     * it, real time first; empty when none does.
     */
    public Optional<Timing> parseTiming(String text) {
        Optional<Timing> real = Timing.of(realTime, text);
        if (real.isPresent()) {
            return real;
        }
        return byId.values().stream()
                .filter(clock -> !clock.equals(realTime))
                .map(clock -> Timing.of(clock, text))
                .flatMap(Optional::stream)
                .findFirst();
    }

    /** Real time. */
    public TimerClock realTime() {
        return realTime;
    }

    private static String normalise(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
