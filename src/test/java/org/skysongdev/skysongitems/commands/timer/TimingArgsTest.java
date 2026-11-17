package org.skysongdev.skysongitems.commands.timer;

import org.junit.jupiter.api.Test;
import org.skysongdev.skysongitems.timer.RealTimeClock;
import org.skysongdev.skysongitems.timer.TimerClocks;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests reading the optional time after a command's own arguments. */
class TimingArgsTest {
    private final TimerClocks clocks = new TimerClocks(new RealTimeClock(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC)));

    private TimingArgs read(String... args) {
        return TimingArgs.read(args, 1, clocks);
    }

    @Test
    void readsNoTimeABareTimeAfterAndEvery() {
        assertFalse(read("honed").isTimed() || read("honed").isLater() || read("honed").isBad());

        TimingArgs bare = read("honed", "1D12h");
        assertTrue(bare.isTimed());
        assertEquals(" for 1d12h", bare.forText());
        assertEquals(36 * 3_600_000L, bare.timing().dueFromNow());

        assertEquals("after 2d", read("honed", "after", "2d").laterText());
        TimingArgs every = read("honed", "EVERY", "1mo");
        assertTrue(every.isLater() && every.repeats());
        assertEquals("every 1mo", every.laterText());
    }

    @Test
    void aTimeThatIsNotOneOrIsMissingIsBad() {
        assertTrue(read("honed", "soon").isBad());
        assertTrue(read("honed", "after").isBad());
        assertTrue(read("honed", "every", "1x").isBad());
    }

    @Test
    void suggestsTheWordsThenTimes() {
        assertEquals(List.of("every"), TimingArgs.suggestions(new String[]{"honed", "e"}, 1));
        assertEquals(List.of("1h", "1mo"), TimingArgs.suggestions(new String[]{"honed", "after", "1"}, 1));
        assertEquals(List.of(), TimingArgs.suggestions(new String[]{"honed", "2d", ""}, 1));
    }
}
