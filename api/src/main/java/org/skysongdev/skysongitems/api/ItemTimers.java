package org.skysongdev.skysongitems.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reads and cancels the timers on SkysongItems items: changes stored on an item that are
 * made once their time comes, such as a trait given for 24 hours being removed.
 *
 * <p>Timers run lazily: an item's due timers are applied when a player deals with it (opens
 * an inventory holding it, clicks in one, uses it, or runs a {@code /si} command on it), never
 * on a clock. So an item nobody touched may still carry timers whose time has passed;
 * {@link #list} shows them as they are stored.
 *
 * <p>Cancelling follows the same rules as the other writes: it only accepts SkysongItems
 * items, except through {@link SkysongItemsApi#detached()}.
 *
 * <p>Must be called on the server's main thread, and throws {@link IllegalStateException} otherwise.
 *
 * @since 1.7.0
 */
@ApiStatus.NonExtendable
public interface ItemTimers {
    /** The item's timers, in the order they are stored; empty for null, air or an item without timers. */
    @NotNull List<ItemTimer> list(@Nullable ItemStack item);

    /**
     * Cancels one of the item's timers.
     *
     * @param index its position in {@link #list}, from 0
     * @return whether there was a timer at that position
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item
     */
    boolean cancel(ItemStack item, int index);

    /**
     * Cancels every timer on the item.
     *
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item
     */
    void clear(ItemStack item);
}
