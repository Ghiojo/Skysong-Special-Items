package org.skysongdev.skysongitems.timer;

import java.util.Locale;
import java.util.Optional;

/**
 * A time staff typed after a command, such as {@code 24h}, read by the clock it counts in.
 *
 * @param clock the clock the time counts in
 * @param step  the time, as that clock steps
 * @param text  the time as typed, which timers keep as their span
 */
public record Timing(TimerClock clock, TimerStep step, String text) {
    /** The time read by this clock; empty when the clock doesn't understand it. */
    static Optional<Timing> of(TimerClock clock, String text) {
        return clock.parseStep(text).map(step -> new Timing(clock, step, text.toLowerCase(Locale.ROOT)));
    }

    /** When this time from now ends, as a position of its clock. */
    public long dueFromNow() {
        return step.after(clock.now());
    }
}
