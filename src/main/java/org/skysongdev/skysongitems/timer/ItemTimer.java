package org.skysongdev.skysongitems.timer;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * One timer stored on an item: once its {@link #clock()} reaches {@link #due()}, the action
 * named {@link #action()} is applied to the item. A repeating timer is then due again
 * {@link #every()} later, counted from when it was due rather than from when it ran, so it
 * keeps to its schedule however late it is checked.
 *
 * <p>Clock and action are stored by id, so a timer whose clock or action isn't registered,
 * such as one from a plugin that was removed, is kept as it is and waits.
 *
 * @param clock  the id of the {@link TimerClock} it counts in, such as {@code real}
 * @param due    when it runs, as a position of that clock
 * @param action the id of the {@link TimerAction} it applies, such as {@code remove_trait}
 * @param target the tag or trait the action is about, or null for actions without one
 * @param value  the value or amount the action takes, or null for actions without one
 * @param span   the time the timer was given when it was made, such as {@code 24h}, or null
 * @param every  how often it repeats, such as {@code 1mo}, or null for a timer that runs once
 */
public record ItemTimer(String clock, long due, String action, @Nullable String target, @Nullable String value,
                        @Nullable String span, @Nullable String every) {
    public ItemTimer {
        Objects.requireNonNull(clock, "clock");
        Objects.requireNonNull(action, "action");
    }

    /** Whether the timer repeats after running. */
    public boolean repeats() {
        return every != null;
    }

    /** The same timer, due at another position of its clock. */
    public ItemTimer withDue(long newDue) {
        return new ItemTimer(clock, newDue, action, target, value, span, every);
    }
}
