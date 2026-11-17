package org.skysongdev.skysongitems.timer;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** The clocks timers can count in, by id. Real time is always there; other plugins add theirs. */
public final class TimerClocks {
    private final Map<String, TimerClock> byId = new HashMap<>();
    private final Map<String, String> owners = new HashMap<>();
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

    /**
     * Adds a clock another plugin counts, under that plugin's name.
     *
     * @throws IllegalArgumentException when the id is that of real time
     */
    public void register(TimerClock clock, String owner) {
        register(clock);
        owners.put(normalise(clock.id()), owner);
    }

    /** The plugin that registered the clock with this id, or null for real time and unknown ids. */
    public String ownerOf(String id) {
        return owners.get(normalise(id));
    }

    /** Drops the clocks a plugin registered, such as when it is disabled. Timers in them wait. */
    public void removeOwnedBy(String owner) {
        owners.entrySet().removeIf(entry -> {
            boolean owned = entry.getValue().equals(owner);
            if (owned) {
                byId.remove(entry.getKey());
            }
            return owned;
        });
    }

    /** Drops every clock other plugins registered, before they register them again on a reload. */
    public void removeAllOwned() {
        owners.keySet().forEach(byId::remove);
        owners.clear();
    }

    /** Real time. */
    public TimerClock realTime() {
        return realTime;
    }

    private static String normalise(String id) {
        return id.toLowerCase(Locale.ROOT);
    }
}
