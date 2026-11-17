package org.skysongdev.skysongitems.commands.timer;

import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.timer.TimerClocks;
import org.skysongdev.skysongitems.timer.Timing;

import java.util.List;
import java.util.Optional;

/**
 * Reads the optional time typed after a command's own arguments, such as the {@code 24h} of
 * {@code /si trait add honed 24h}, so no command reads times itself.
 *
 * @param timing the time typed, or null when none was
 * @param badTime the text typed where the time goes when it isn't one, or null
 */
public record TimingArgs(@Nullable Timing timing, @Nullable String badTime) {
    /** The message for a time that isn't one. */
    public static final String BAD_TIME = "Invalid time! Use a number and a unit, such as 30m, 24h, 7d, 1mo or 1d12h.";

    private static final List<String> EXAMPLES = List.of("30m", "1h", "24h", "7d", "1mo");

    /** Example times starting with what was typed, for tab completion. */
    public static List<String> suggestions(String typed) {
        return EXAMPLES.stream().filter(example -> example.startsWith(typed)).toList();
    }

    /**
     * Reads the argument after the command's own ones.
     *
     * @param args      the command's arguments
     * @param ownCount  how many arguments the command itself takes before the time
     */
    public static TimingArgs read(String[] args, int ownCount, TimerClocks clocks) {
        if (args.length <= ownCount) {
            return new TimingArgs(null, null);
        }
        String text = args[ownCount];
        Optional<Timing> timing = clocks.parseTiming(text);
        return timing.map(found -> new TimingArgs(found, null)).orElseGet(() -> new TimingArgs(null, text));
    }

    /** Whether a time was typed but couldn't be read. */
    public boolean isBad() {
        return badTime != null;
    }

    /** Whether a time was typed and read. */
    public boolean isTimed() {
        return timing != null;
    }

    /** {@code " for 24h"} when a time was typed, otherwise nothing, for confirmation messages. */
    public String forText() {
        return timing == null ? "" : " for " + timing.text();
    }
}
