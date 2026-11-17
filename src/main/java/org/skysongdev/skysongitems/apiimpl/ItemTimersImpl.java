package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.ItemTimer;
import org.skysongdev.skysongitems.api.ItemTimers;
import org.skysongdev.skysongitems.api.TimerChange;
import org.skysongdev.skysongitems.timer.ItemTimerScheduler;
import org.skysongdev.skysongitems.timer.ItemTimerStore;
import org.skysongdev.skysongitems.timer.TimerAction;
import org.skysongdev.skysongitems.timer.TimerActions;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads, adds and cancels the timers stored on items, through the {@link ItemTimerStore} and
 * {@link ItemTimerScheduler}, the same as the commands.
 */
final class ItemTimersImpl implements ItemTimers {
    private final ItemWrites.ItemCheck check;
    private final ItemEditor editor;
    private final TimerActions actions;
    private final TimedWrites timed;

    ItemTimersImpl(ItemWrites.ItemCheck check, ItemEditor editor, TimerActions actions, TimedWrites timed) {
        this.check = check;
        this.editor = editor;
        this.actions = actions;
        this.timed = timed;
    }

    @Override
    public @NotNull ItemTimer after(ItemStack item, TimerChange change, String time) {
        return schedule(item, change, time, false);
    }

    @Override
    public @NotNull ItemTimer every(ItemStack item, TimerChange change, String time) {
        return schedule(item, change, time, true);
    }

    /** Checks the change on a copy of the item, then stores it as a timer in place of the same change's earlier one. */
    private ItemTimer schedule(ItemStack item, TimerChange change, String time, boolean repeats) {
        check.require(item);
        TimerAction action = actions.find(change.action())
                .orElseThrow(() -> new IllegalArgumentException("There is no timer action " + change.action()));
        org.skysongdev.skysongitems.timer.ItemTimer timer =
                ItemTimerScheduler.timer(timed.timing(time), repeats, action.id(), change.target(), change.value());
        ItemWrites.requireDone(action.check(editor, item, timer), "timer " + action.describe(timer));
        return toApi(ItemTimerScheduler.replace(item, timer));
    }

    @Override
    public @NotNull List<ItemTimer> list(ItemStack item) {
        StoredValues.requireMainThread();
        if (item == null || item.getType().isAir()) {
            return List.of();
        }
        return ItemTimerStore.read(item.getPersistentDataContainer()).stream().map(ItemTimersImpl::toApi).toList();
    }

    @Override
    public boolean cancel(ItemStack item, int index) {
        check.require(item);
        List<org.skysongdev.skysongitems.timer.ItemTimer> timers =
                new ArrayList<>(ItemTimerStore.read(item.getPersistentDataContainer()));
        if (index < 0 || index >= timers.size()) {
            return false;
        }
        timers.remove(index);
        ItemTimerStore.write(item, timers);
        return true;
    }

    @Override
    public void clear(ItemStack item) {
        check.require(item);
        ItemTimerStore.write(item, List.of());
    }

    private static ItemTimer toApi(org.skysongdev.skysongitems.timer.ItemTimer timer) {
        return new ItemTimer(timer.clock(), timer.due(), timer.action(), timer.target(), timer.value(),
                timer.span(), timer.every());
    }
}
