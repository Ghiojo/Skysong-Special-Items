package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.ItemTimer;
import org.skysongdev.skysongitems.api.ItemTimers;
import org.skysongdev.skysongitems.timer.ItemTimerStore;

import java.util.ArrayList;
import java.util.List;

/** Reads the timers stored on items, and cancels them, through the {@link ItemTimerStore}. */
final class ItemTimersImpl implements ItemTimers {
    private final ItemWrites.ItemCheck check;

    ItemTimersImpl(ItemWrites.ItemCheck check) {
        this.check = check;
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
