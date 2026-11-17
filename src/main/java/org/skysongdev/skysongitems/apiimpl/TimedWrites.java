package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.timer.ItemTimerScheduler;
import org.skysongdev.skysongitems.timer.TimerClocks;
import org.skysongdev.skysongitems.timer.Timing;

/** Reads the times the API's timed writes take and schedules what follows the change, as the commands do. */
final class TimedWrites {
    private final TimerClocks clocks;

    TimedWrites(TimerClocks clocks) {
        this.clocks = clocks;
    }

    /**
     * The time as staff would type it after a command, such as {@code 24h}.
     *
     * @throws IllegalArgumentException when it isn't one
     */
    Timing timing(String time) {
        if (time == null) {
            throw new IllegalArgumentException("A time is required, such as 24h");
        }
        return clocks.parseTiming(time)
                .orElseThrow(() -> new IllegalArgumentException("Not a time: " + time + " (use for example 30m, 24h, 7d or 1mo)"));
    }

    /**
     * With a time, applies the action once it has passed, restarting an earlier such timer;
     * without one, cancels any earlier such timer, since the change is now for good.
     */
    void then(ItemStack item, @Nullable Timing timing, String action, String target) {
        ItemTimerScheduler.replaceOrCancel(item, timing, action, target);
    }

    /** Applies the undoing change once the time has passed, next to any earlier such timer, so these stack. */
    void undoLater(ItemStack item, Timing timing, String action, @Nullable String target, @Nullable String value) {
        ItemTimerScheduler.add(item, ItemTimerScheduler.timer(timing, false, action, target, value));
    }
}
