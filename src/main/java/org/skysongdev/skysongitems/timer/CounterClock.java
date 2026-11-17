package org.skysongdev.skysongitems.timer;

import java.util.Locale;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A clock another plugin counts, such as the seasons of a calendar plugin: it only says how
 * many have passed so far, and timers in it are due when that count reaches theirs. Its unit
 * is its id, so a time in it reads {@code 2season}. The count is asked for only when an
 * item's timers are checked, so the plugin never has to tick it, and it must never go down.
 */
public final class CounterClock implements TimerClock {
    private final String id;
    private final LongSupplier counter;
    private final Pattern time;

    /**
     * @param id      the unit, lowercase letters only, such as {@code season}
     * @param counter how many units have passed so far
     */
    public CounterClock(String id, LongSupplier counter) {
        this.id = id.toLowerCase(Locale.ROOT);
        this.counter = counter;
        this.time = Pattern.compile("(\\d{1,9})" + Pattern.quote(this.id), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public long now() {
        return counter.getAsLong();
    }

    /** Reads a whole number of units, such as {@code 2season}; empty for anything else, or 0. */
    @Override
    public Optional<CounterStep> parseStep(String text) {
        Matcher matcher = time.matcher(text);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        long units = Long.parseLong(matcher.group(1));
        return units > 0 ? Optional.of(new CounterStep(units)) : Optional.empty();
    }

    @Override
    public String describeWait(long from, long to) {
        return to > from ? (to - from) + id : "";
    }

    /** A number of units, every one of them the same length. */
    public record CounterStep(long units) implements TimerStep {
        @Override
        public long after(long position) {
            return position + units;
        }

        @Override
        public long countDue(long due, long now) {
            return now < due ? 0 : (now - due) / units + 1;
        }

        @Override
        public long advance(long position, long steps) {
            return position + units * steps;
        }
    }
}
