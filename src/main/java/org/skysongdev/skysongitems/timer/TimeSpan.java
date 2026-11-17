package org.skysongdev.skysongitems.timer;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A length of time as staff type it in commands: a number and a unit, such as {@code 24h},
 * or several of them together, such as {@code 1d12h}.
 *
 * <p>The units are {@code s} (seconds), {@code m} (minutes), {@code h} (hours), {@code d}
 * (days), {@code w} (weeks), {@code mo} (months) and {@code y} (years). Months and years
 * are calendar ones: a month from January 15 is February 15, whatever the month's length.
 * Calendar dates are counted in UTC, so a server's time zone never moves a timer. A span
 * with months steps through the calendar a month at a time, at most 12 000 steps in 1000 years.
 *
 * @param months  the whole months, years counted as 12 months
 * @param seconds the rest, in seconds
 */
public record TimeSpan(long months, long seconds) implements TimerStep {
    /** The longest span accepted, so a typo can't push a timer past the end of time. */
    private static final long MAX_MONTHS = 12L * 1000;
    private static final long MAX_SECONDS = MAX_MONTHS * 31 * 24 * 3600;
    private static final Pattern PART = Pattern.compile("(\\d{1,9})(mo|[smhdwy])");

    public TimeSpan {
        if (months < 0 || seconds < 0 || months + seconds == 0) {
            throw new IllegalArgumentException("A time span must be longer than nothing");
        }
    }

    /**
     * Reads a time span such as {@code 30m}, {@code 1mo} or {@code 1d12h}, ignoring case.
     *
     * @return the span, or empty when the text isn't one, or is zero or over 1000 years long
     */
    public static Optional<TimeSpan> parse(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        Matcher part = PART.matcher(lower);
        long months = 0;
        long seconds = 0;
        int end = 0;
        while (part.find() && part.start() == end) {
            long amount = Long.parseLong(part.group(1));
            months += amount * monthsPer(part.group(2));
            seconds += amount * secondsPer(part.group(2));
            end = part.end();
        }
        boolean readAll = end > 0 && end == lower.length();
        return readAll && isAllowed(months, seconds) ? Optional.of(new TimeSpan(months, seconds)) : Optional.empty();
    }

    private static boolean isAllowed(long months, long seconds) {
        return months + seconds > 0 && months <= MAX_MONTHS && seconds <= MAX_SECONDS;
    }

    private static long monthsPer(String unit) {
        return switch (unit) {
            case "y" -> 12;
            case "mo" -> 1;
            default -> 0;
        };
    }

    private static long secondsPer(String unit) {
        return switch (unit) {
            case "m" -> 60;
            case "h" -> 3600;
            case "d" -> 86_400;
            case "w" -> 604_800;
            case "s" -> 1;
            default -> 0;
        };
    }

    /** The moment this long after the given one, both in milliseconds since 1970 (UTC). */
    @Override
    public long after(long epochMillis) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneOffset.UTC)
                .plusMonths(months).plusSeconds(seconds).toInstant().toEpochMilli();
    }

    /** Counted with a division when the span has a fixed length, as it does without months. */
    @Override
    public long countDue(long due, long now) {
        if (months > 0 || due > now) {
            return TimerStep.super.countDue(due, now);
        }
        return (now - due) / fixedMillis() + 1;
    }

    /** Counted with a multiplication when the span has a fixed length, as it does without months. */
    @Override
    public long advance(long position, long steps) {
        return months > 0 ? TimerStep.super.advance(position, steps) : position + steps * fixedMillis();
    }

    private long fixedMillis() {
        return seconds * 1000;
    }

    /** The span as {@link #parse} reads it, largest unit first, such as {@code 1y2mo3d}. */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        append(text, months / 12, "y");
        append(text, months % 12, "mo");
        append(text, seconds / 86_400, "d");
        append(text, seconds % 86_400 / 3600, "h");
        append(text, seconds % 3600 / 60, "m");
        append(text, seconds % 60, "s");
        return text.toString();
    }

    private static void append(StringBuilder text, long amount, String unit) {
        if (amount > 0) {
            text.append(amount).append(unit);
        }
    }
}
