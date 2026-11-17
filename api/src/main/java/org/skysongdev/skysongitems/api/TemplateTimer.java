package org.skysongdev.skysongitems.api;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A timer as a template or saved item keeps it: a change and how long after an item is made
 * it happens. Templates keep spans, never dates, so every item {@link ItemTemplate#create()}
 * makes starts its own clock.
 *
 * @param change  what the timer does
 * @param time    how long after the item is made it runs, such as {@code 24h}
 * @param repeats whether it then runs again every {@code time}
 * @param clock   the clock it counts in: {@code real} for real time, or the id of a clock
 *                another plugin registered
 * @since 1.12.0
 */
public record TemplateTimer(@NotNull TimerChange change, @NotNull String time, boolean repeats, @NotNull String clock) {
    public TemplateTimer {
        Objects.requireNonNull(change, "change");
        Objects.requireNonNull(time, "time");
        Objects.requireNonNull(clock, "clock");
    }
}
