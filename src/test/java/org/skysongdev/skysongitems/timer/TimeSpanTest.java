package org.skysongdev.skysongitems.timer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests reading the time spans staff type, such as {@code 24h} or {@code 1mo}. */
class TimeSpanTest {
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "30s, 0, 30",
            "5m, 0, 300",
            "24h, 0, 86400",
            "2d, 0, 172800",
            "1w, 0, 604800",
            "1mo, 1, 0",
            "2y, 24, 0",
            "1d12h, 0, 129600",
            "1y1mo1d, 13, 86400",
            "1MO, 1, 0"
    })
    void readsEachUnitAndCombinations(String text, long months, long seconds) {
        assertEquals(Optional.of(new TimeSpan(months, seconds)), TimeSpan.parse(text));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"", "5", "h", "5x", "0s", "0mo", "1h 30m", "-1h", "1h-", "1001y", "12001mo"})
    void rejectsAnythingElse(String text) {
        assertTrue(TimeSpan.parse(text).isEmpty());
    }

    private static long millis(String instant) {
        return Instant.parse(instant).toEpochMilli();
    }

    private static TimeSpan span(String text) {
        return TimeSpan.parse(text).orElseThrow();
    }

    @Test
    void aMonthIsACalendarMonthEndingOnTheLastDayWhenTheDateIsMissing() {
        assertEquals(millis("2026-02-28T10:00:00Z"), span("1mo").after(millis("2026-01-31T10:00:00Z")));
    }

    @Test
    void monthsAreAddedBeforeTheRest() {
        assertEquals(millis("2026-03-01T10:00:00Z"), span("1mo1d").after(millis("2026-01-31T10:00:00Z")));
    }

    @Test
    void countsTheStepsThatCameDueWithOneDivisionForFixedSpans() {
        long due = millis("2026-01-01T00:00:00Z");

        // Due on Jan 1 itself, then every hour up to and including 10:00.
        assertEquals(11, span("1h").countDue(due, millis("2026-01-01T10:30:00Z")));
        assertEquals(0, span("1h").countDue(due, due - 1));
        assertEquals(millis("2026-01-01T11:00:00Z"), span("1h").advance(due, 11));
    }

    @Test
    void countsCalendarMonthsOnTheCalendar() {
        long due = millis("2026-08-17T09:00:00Z");

        assertEquals(4, span("1mo").countDue(due, millis("2026-11-20T00:00:00Z")));
        assertEquals(millis("2026-12-17T09:00:00Z"), span("1mo").advance(due, 4));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({"1y2mo3d4h5m6s, 1y2mo3d4h5m6s", "90m, 1h30m", "14mo, 1y2mo", "1w, 7d"})
    void printsTheShortestFormParseReads(String text, String printed) {
        assertEquals(printed, TimeSpan.parse(text).orElseThrow().toString());
    }
}
