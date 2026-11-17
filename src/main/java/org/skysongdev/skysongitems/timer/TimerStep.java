package org.skysongdev.skysongitems.timer;

/**
 * The length of one step of a {@link TimerClock}, such as {@code 1mo} of real time or one
 * season. Positions are the clock's own: milliseconds for real time, a count for others.
 */
public interface TimerStep {
    /** The position one step after the given one. */
    long after(long position);

    /**
     * How many steps that start at {@code due} have come by {@code now}: 0 when {@code due}
     * is still ahead, otherwise 1 for {@code due} itself plus every whole step after it.
     * Clocks whose steps all have one length should override this with a division.
     */
    default long countDue(long due, long now) {
        long count = 0;
        for (long position = due; position <= now; position = after(position)) {
            count++;
        }
        return count;
    }

    /**
     * The position the given number of steps after the given one. Clocks whose steps all
     * have one length should override this with a multiplication.
     */
    default long advance(long position, long steps) {
        long result = position;
        for (long i = 0; i < steps; i++) {
            result = after(result);
        }
        return result;
    }
}
