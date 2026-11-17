package org.skysongdev.skysongitems.timer;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests a clock another plugin counts, such as seasons. */
class CounterClockTest {
    private final AtomicLong seasons = new AtomicLong(7);
    private final CounterClock clock = new CounterClock("season", seasons::get);

    @Test
    void readsWholeNumbersOfItsUnitOnly() {
        assertEquals(Optional.of(new CounterClock.CounterStep(2)), clock.parseStep("2Season"));
        assertEquals(Optional.empty(), clock.parseStep("0season"));
        assertEquals(Optional.empty(), clock.parseStep("2d"));
        assertEquals(Optional.empty(), clock.parseStep("season"));
    }

    @Test
    void countsMissedStepsAndTheWaitInItsOwnUnit() {
        CounterClock.CounterStep twoSeasons = new CounterClock.CounterStep(2);

        assertEquals(7, clock.now());
        assertEquals(0, twoSeasons.countDue(8, clock.now()));
        assertEquals(3, twoSeasons.countDue(2, clock.now()));
        assertEquals(8, twoSeasons.advance(2, 3));
        assertEquals("3season", clock.describeWait(7, 10));
        assertEquals("", clock.describeWait(7, 7));
    }
}
