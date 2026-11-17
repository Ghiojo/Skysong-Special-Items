package org.skysongdev.skysongitems.timer;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Writes a saved item's timers into its YAML file and reads them back, under {@code timers:},
 * one entry per timer:
 * <pre>
 * timers:
 * - action: remove_trait
 *   target: honed
 *   after: 24h
 * - action: transform
 *   every: 12h
 * </pre>
 * {@code value} is written for actions that take one, and {@code clock} for timers that
 * don't count in real time. An entry without an action or a time is skipped with a warning.
 */
public final class TimerTemplateYaml {
    private static final String KEY = "timers";
    private static final String AFTER = "after";
    private static final String EVERY = "every";

    private TimerTemplateYaml() {
    }

    /** Writes the timers, or nothing when there are none. */
    public static void write(ConfigurationSection yaml, List<TimerTemplate> timers) {
        if (timers.isEmpty()) {
            return;
        }
        List<Map<String, Object>> entries = new ArrayList<>();
        for (TimerTemplate timer : timers) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("action", timer.action());
            putIfPresent(entry, "target", timer.target());
            putIfPresent(entry, "value", timer.value());
            entry.put(timer.repeats() ? EVERY : AFTER, timer.span());
            if (!RealTimeClock.ID.equals(timer.clock())) {
                entry.put("clock", timer.clock());
            }
            entries.add(entry);
        }
        yaml.set(KEY, entries);
    }

    private static void putIfPresent(Map<String, Object> entry, String key, String value) {
        if (value != null) {
            entry.put(key, value);
        }
    }

    /**
     * Reads the timers.
     *
     * @param file named in warnings about entries that aren't timers
     */
    public static List<TimerTemplate> read(ConfigurationSection yaml, String file, Logger logger) {
        List<TimerTemplate> timers = new ArrayList<>();
        for (Map<?, ?> entry : yaml.getMapList(KEY)) {
            TimerTemplate timer = readEntry(entry);
            if (timer == null) {
                logger.warning("Item file " + file + " has a timer without an action and an after or every time: " + entry);
            } else {
                timers.add(timer);
            }
        }
        return timers;
    }

    private static TimerTemplate readEntry(Map<?, ?> entry) {
        String action = text(entry, "action");
        String every = text(entry, EVERY);
        String span = every != null ? every : text(entry, AFTER);
        if (action == null || span == null) {
            return null;
        }
        String clock = text(entry, "clock");
        return new TimerTemplate(clock == null ? RealTimeClock.ID : clock, action.toLowerCase(Locale.ROOT),
                text(entry, "target"), text(entry, "value"), span, every != null);
    }

    private static String text(Map<?, ?> entry, String key) {
        Object value = entry.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
