package org.skysongdev.skysongitems.timer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Applies the timers that are due on an item.
 *
 * <p>Nothing watches the clock: an item's timers are only looked at when something calls
 * {@link #run}, which {@link ItemTimerTriggers} does when a player deals with the item.
 * Every timer whose time has come since is then applied, oldest first.
 *
 * <p>A repeating timer that missed several repeats isn't replayed one by one: the number
 * missed is counted and handed to its action once, so a check costs the same however long
 * the item sat untouched. Its next time stays on its original schedule, counted from when it
 * was due, so a monthly timer stays on the same day of the month.
 *
 * <p>A timer whose clock or action isn't registered is kept as it is and waits.
 */
public final class ItemTimerRunner {
    private final TimerClocks clocks;
    private final TimerActions actions;
    private final Supplier<ItemEditor> editor;

    /**
     * @param editor makes each change, so it is checked, drawn into the lore and reported
     *               with change events like the same change made by a command
     */
    public ItemTimerRunner(TimerClocks clocks, TimerActions actions, Supplier<ItemEditor> editor) {
        this.clocks = clocks;
        this.actions = actions;
        this.editor = editor;
    }

    /** One timer that came due, how many times, and what stays on the item after it. */
    private record DueTimer(ItemTimer timer, TimerAction action, long times, @Nullable ItemTimer next) {
    }

    /**
     * Applies the item's due timers to it.
     *
     * @param holder the player holding the item, named by the change events, or null for an
     *               item in a container
     * @return the item afterwards, which the caller puts back where it was; empty when no
     *         timer was due and the item is unchanged
     */
    public Optional<ItemStack> run(@Nullable ItemStack item, @Nullable Player holder) {
        if (item == null || item.getType().isAir() || !ItemTimerStore.hasTimers(item.getPersistentDataContainer())) {
            return Optional.empty();
        }
        List<DueTimer> due = new ArrayList<>();
        List<ItemTimer> kept = new ArrayList<>();
        for (ItemTimer timer : ItemTimerStore.read(item.getPersistentDataContainer())) {
            dueTimer(timer).ifPresentOrElse(due::add, () -> kept.add(timer));
        }
        if (due.isEmpty()) {
            return Optional.empty();
        }
        due.sort(Comparator.comparingLong(dueTimer -> dueTimer.timer().due()));
        due.stream().map(DueTimer::next).filter(Objects::nonNull).forEach(kept::add);
        ItemTimerStore.write(item, kept);

        TimerContext context = new TimerContext(editor.get().editedBy(holder, ItemChangeCause.TIMER), holder);
        ItemStack current = item;
        for (DueTimer timer : due) {
            current = timer.action().apply(context, current, timer.timer(), timer.times());
        }
        return Optional.of(current);
    }

    /** The timer as it comes due now, or empty when it isn't due or can't run. */
    private Optional<DueTimer> dueTimer(ItemTimer timer) {
        Optional<TimerClock> clock = clocks.find(timer.clock());
        Optional<TimerAction> action = actions.find(timer.action());
        if (clock.isEmpty() || action.isEmpty()) {
            return Optional.empty();
        }
        long now = clock.get().now();
        if (timer.due() > now) {
            return Optional.empty();
        }
        if (!timer.repeats()) {
            return Optional.of(new DueTimer(timer, action.get(), 1, null));
        }
        return clock.get().parseStep(timer.every()).map(step -> {
            long times = step.countDue(timer.due(), now);
            return new DueTimer(timer, action.get(), times, timer.withDue(step.advance(timer.due(), times)));
        });
    }

    /**
     * Applies the due timers of every item in the inventory.
     *
     * @param holder the player whose inventory it is, or null for a container
     * @return how many items changed
     */
    public int runAll(Inventory inventory, @Nullable Player holder) {
        int changed = 0;
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            Optional<ItemStack> after = run(inventory.getItem(slot), holder);
            if (after.isPresent()) {
                // Some inventories hand out copies, so put the changed item back.
                inventory.setItem(slot, after.get());
                changed++;
            }
        }
        return changed;
    }
}
