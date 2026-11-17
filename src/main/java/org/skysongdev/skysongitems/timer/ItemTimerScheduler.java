package org.skysongdev.skysongitems.timer;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adds timers to items and cancels them. A tag or trait has at most one timer of each action:
 * scheduling one again replaces the earlier one, so honing an item that is already Honed
 * restarts its time instead of stacking a second removal.
 */
public final class ItemTimerScheduler {
    private ItemTimerScheduler() {
    }

    /**
     * Adds a timer that applies the action once, when the time has passed, in place of any
     * timer with the same action and target.
     *
     * @return the timer added
     */
    public static ItemTimer replaceOnce(ItemStack item, Timing timing, String action, String target, @Nullable String value) {
        ItemTimer timer = new ItemTimer(timing.clock().id(), timing.dueFromNow(), action, target, value, timing.text(), null);
        List<ItemTimer> timers = without(item, action, target);
        timers.add(timer);
        ItemTimerStore.write(item, timers);
        return timer;
    }

    /**
     * What a timed command or API call does after its change: with a time, applies the action
     * once it has passed, restarting any earlier such timer; without one, the change is for
     * good, so any earlier such timer is cancelled.
     *
     * @param timing the time given, or null for none
     * @return whether the item's timers changed
     */
    public static boolean replaceOrCancel(ItemStack item, @Nullable Timing timing, String action, String target) {
        if (timing == null) {
            return cancel(item, action, target);
        }
        replaceOnce(item, timing, action, target, null);
        return true;
    }

    /**
     * Cancels the item's timers with this action and target.
     *
     * @return whether it had any
     */
    public static boolean cancel(ItemStack item, String action, String target) {
        if (!ItemTimerStore.hasTimers(item.getPersistentDataContainer())) {
            return false;
        }
        List<ItemTimer> timers = without(item, action, target);
        if (timers.size() == ItemTimerStore.read(item.getPersistentDataContainer()).size()) {
            return false;
        }
        ItemTimerStore.write(item, timers);
        return true;
    }

    /** The item's timers, leaving out those with this action and target. */
    private static List<ItemTimer> without(ItemStack item, String action, String target) {
        List<ItemTimer> timers = new ArrayList<>(ItemTimerStore.read(item.getPersistentDataContainer()));
        timers.removeIf(timer -> action.equals(timer.action()) && target.equalsIgnoreCase(timer.target()));
        return timers;
    }
}
