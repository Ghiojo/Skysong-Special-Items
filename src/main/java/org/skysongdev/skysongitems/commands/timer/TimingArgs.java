package org.skysongdev.skysongitems.commands.timer;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.timer.TimerClocks;
import org.skysongdev.skysongitems.timer.Timing;
import org.skysongdev.skysongitems.utils.Constants;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * Reads the optional time typed after a command's own arguments, so no command reads times
 * itself: {@code 24h} makes the change for a while, {@code after 2d} makes it later and
 * {@code every 1mo} makes it again and again.
 *
 * @param kind    which of the three was typed, or null when no time was
 * @param timing  the time typed, or null when none was or it couldn't be read
 * @param badTime the text typed where the time goes when it isn't one, or null
 */
public record TimingArgs(@Nullable Kind kind, @Nullable Timing timing, @Nullable String badTime) {
    /** The message for a time that isn't one. */
    public static final String BAD_TIME = "Invalid time! Use a number and a unit, such as 30m, 24h, 7d, 1mo or 1d12h.";
    /** The message for a time typed by a player who isn't staff. */
    public static final String STAFF_ONLY = "Only staff can change items over time.";
    /** The permission needed to type a time after a command. */
    public static final String TIME_PERMISSION = "skysongspecialitems.cit";

    private static final List<String> EXAMPLES = List.of("30m", "1h", "24h", "7d", "1mo");
    private static final List<String> WORDS = List.of("after", "every");

    /** How the time is used. */
    public enum Kind {
        /** A bare time, such as {@code 24h}: the change is made now and undone later. */
        FOR,
        /** {@code after 2d}: the change is made once the time has passed. */
        AFTER,
        /** {@code every 1mo}: the change is made each time the time passes. */
        EVERY
    }

    /** Example times, and {@code after} and {@code every} where they fit, starting with what was typed. */
    public static List<String> suggestions(String[] args, int ownCount) {
        int typed = args.length - 1 - ownCount;
        String last = args[args.length - 1].toLowerCase(Locale.ROOT);
        if (typed == 0) {
            return Stream.concat(WORDS.stream(), EXAMPLES.stream()).filter(word -> word.startsWith(last)).toList();
        }
        if (typed == 1 && WORDS.contains(args[ownCount].toLowerCase(Locale.ROOT))) {
            return EXAMPLES.stream().filter(example -> example.startsWith(last)).toList();
        }
        return List.of();
    }

    /**
     * Reads the arguments after the command's own ones.
     *
     * @param args     the command's arguments
     * @param ownCount how many arguments the command itself takes before the time
     */
    public static TimingArgs read(String[] args, int ownCount, TimerClocks clocks) {
        if (args.length <= ownCount) {
            return new TimingArgs(null, null, null);
        }
        Kind kind = kindNamed(args[ownCount]);
        int timeIndex = Kind.FOR.equals(kind) ? ownCount : ownCount + 1;
        String text = timeIndex < args.length ? args[timeIndex] : "";
        Optional<Timing> timing = clocks.parseTiming(text);
        return timing.map(found -> new TimingArgs(kind, found, null)).orElseGet(() -> new TimingArgs(kind, null, text));
    }

    /**
     * Reads the time after the command's own arguments, or tells the player why it can't be
     * used: it isn't a time, or it is a bare time on a command that only takes
     * {@code after} or {@code every}.
     *
     * @param laterOnly whether the command only takes {@code after} or {@code every}
     * @return the time read, or null when the player was told it can't be used
     */
    public static @Nullable TimingArgs readOrRefuse(Player player, String[] args, int ownCount, boolean laterOnly) {
        TimingArgs timing = read(args, ownCount, getPlugin().getTimerClocks());
        String refusal = timing.isBad() ? BAD_TIME : null;
        if (laterOnly && timing.isTimed()) {
            refusal = LaterEdits.LATER_ONLY;
        }
        if (refusal != null) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>" + refusal));
            return null;
        }
        return timing;
    }

    /**
     * Tells a player who isn't staff that they can't type a time, for the commands players
     * may run too, such as {@code /si hp reduce}: changing an item over time is for staff.
     *
     * @return whether a time was typed by a player who isn't staff, who was told so
     */
    public static boolean refusedForPlayer(Player player, String[] args, int ownCount) {
        if (args.length <= ownCount || player.hasPermission(TIME_PERMISSION)) {
            return false;
        }
        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>" + STAFF_ONLY));
        return true;
    }

    private static Kind kindNamed(String word) {
        return switch (word.toLowerCase(Locale.ROOT)) {
            case "after" -> Kind.AFTER;
            case "every" -> Kind.EVERY;
            default -> Kind.FOR;
        };
    }

    /** Whether a time was typed but couldn't be read. */
    public boolean isBad() {
        return badTime != null;
    }

    /** Whether a bare time was typed and read: the change is made now and undone later. */
    public boolean isTimed() {
        return timing != null && Kind.FOR.equals(kind);
    }

    /** The bare time typed, such as the {@code 24h} of {@code /si trait add honed 24h}, or null when none was. */
    public @Nullable Timing forTiming() {
        return isTimed() ? timing : null;
    }

    /** Whether {@code after} or {@code every} was typed with a time: the change is left to a timer. */
    public boolean isLater() {
        return timing != null && !Kind.FOR.equals(kind);
    }

    /** Whether {@code every} was typed. */
    public boolean repeats() {
        return Kind.EVERY.equals(kind);
    }

    /** {@code " for 24h"} when a bare time was typed, otherwise nothing, for confirmation messages. */
    public String forText() {
        return isTimed() ? " for " + timing.text() : "";
    }

    /** {@code "after 2d"} or {@code "every 1mo"}, for confirmation messages. */
    public String laterText() {
        return isLater() ? kind.name().toLowerCase(Locale.ROOT) + " " + timing.text() : "";
    }
}
