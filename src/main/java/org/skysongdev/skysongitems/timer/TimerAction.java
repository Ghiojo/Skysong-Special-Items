package org.skysongdev.skysongitems.timer;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.utils.ItemEditor;

/**
 * What a timer does to its item once it is due, such as removing a trait. Each kind is one
 * implementation, registered in {@link TimerActions} under the id timers store.
 */
public interface TimerAction {
    /** The id timers store for this action, such as {@code remove_trait}. */
    String id();

    /**
     * Applies the timer's change to the item as many times as given, in one go: a repeating
     * timer that missed several repeats passes how many. An action whose result doesn't
     * depend on the count, such as adding a trait, applies it once. A change that no longer
     * fits the item, such as a trait that was deleted from config, leaves the item alone.
     *
     * @param times how many times the timer came due, at least 1
     * @return the item afterwards: the same one changed in place, or the one replacing it
     */
    ItemStack apply(TimerContext context, ItemStack item, ItemTimer timer, long times);

    /**
     * Whether the timer's change could apply to the item, checked before the timer is
     * stored: refusals such as an unknown tag or a value of the wrong type. A change the item
     * just doesn't need right now, such as removing a trait it doesn't have yet, passes.
     * The item itself is never changed.
     *
     * @return {@link ItemEditor.Result#DONE} when it could apply, otherwise why not
     */
    default ItemEditor.Result check(ItemEditor editor, ItemStack item, ItemTimer timer) {
        return ItemEditor.Result.DONE;
    }

    /** How the timer reads to staff and players, such as {@code trait remove honed}. */
    String describe(ItemTimer timer);
}
