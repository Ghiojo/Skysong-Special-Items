package org.skysongdev.skysongitems.timer;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the timers saved items keep, as spans, and starting them on a new item. */
class TimerTemplateTest {
    private static final long NOW = Instant.parse("2026-11-17T12:00:00Z").toEpochMilli();
    private static final long HOUR = 3_600_000L;
    private static final TimerTemplate HONED_ENDS = new TimerTemplate("real", "remove_trait", "honed", null, "24h", false);
    private static final TimerTemplate MONTHLY = new TimerTemplate("real", "transform", null, null, "1mo", true);

    private final TimerClocks clocks = new TimerClocks(new RealTimeClock(Clock.fixed(Instant.ofEpochMilli(NOW), ZoneOffset.UTC)));

    @Test
    void aTimerOnAnItemIsKeptAsItsOriginalSpanOrRepeat() {
        assertEquals(Optional.of(HONED_ENDS), TimerTemplate.of(new ItemTimer("real", NOW + HOUR, "remove_trait", "honed", null, "24h", null)));
        assertEquals(Optional.of(MONTHLY), TimerTemplate.of(new ItemTimer("real", NOW, "transform", null, null, "1mo", "1mo")));
        assertTrue(TimerTemplate.of(new ItemTimer("real", NOW, "transform", null, null, null, null)).isEmpty());
    }

    @Test
    void startsFromNowOrFromAnOriginOnItsClockAndWaitsOutUnknownClocks() {
        assertEquals(NOW + 24 * HOUR, HONED_ENDS.start(clocks, null).orElseThrow().due());
        assertEquals(NOW - HOUR + 24 * HOUR, HONED_ENDS.start(clocks, new TimerOrigin("real", NOW - HOUR)).orElseThrow().due());
        assertEquals(NOW + 24 * HOUR, HONED_ENDS.start(clocks, new TimerOrigin("season", 3)).orElseThrow().due());
        assertEquals("1mo", MONTHLY.start(clocks, null).orElseThrow().every());

        TimerTemplate seasonal = new TimerTemplate("season", "transform", null, null, "1season", true);
        assertEquals(List.of(), TimerTemplate.startAll(List.of(seasonal), clocks, null));
    }

    @Test
    void yamlKeepsEveryFieldAndSkipsEntriesThatAreNotTimers() {
        YamlConfiguration yaml = new YamlConfiguration();
        TimerTemplate seasonal = new TimerTemplate("season", "add_tag", "arcrot", "2", "1season", true);
        TimerTemplateYaml.write(yaml, List.of(HONED_ENDS, MONTHLY, seasonal));
        yaml.set("timers", appendBroken(yaml));

        assertEquals(List.of(HONED_ENDS, MONTHLY, seasonal), TimerTemplateYaml.read(yaml, "test.yml", Logger.getAnonymousLogger()));
    }

    private static List<Object> appendBroken(YamlConfiguration yaml) {
        List<Object> entries = new ArrayList<>(yaml.getList("timers"));
        entries.add(Map.of("action", "transform"));
        return entries;
    }
}
