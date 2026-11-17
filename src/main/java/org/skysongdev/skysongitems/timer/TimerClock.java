package org.skysongdev.skysongitems.timer;

import java.util.Optional;

/**
 * Something timers count in. {@link RealTimeClock} counts real time; other plugins can add
 * clocks of their own, such as the seasons of a calendar plugin.
 *
 * <p>A clock is only asked where it is when an item's timers are checked, so it never needs
 * to tick: a calendar's clock can simply say how many seasons have passed so far.
 */
public interface TimerClock {
    /** The id timers store to name this clock, such as {@code real}. */
    String id();

    /** Where the clock is now. Never goes backwards. */
    long now();

    /** Reads a step length as staff type it, such as {@code 1mo} or {@code 2season}; empty when it isn't one. */
    Optional<? extends TimerStep> parseStep(String text);

    /**
     * How long from one position to a later one, for showing to players, such as
     * {@code 23h59m}; empty when the later one isn't later.
     */
    String describeWait(long from, long to);
}
