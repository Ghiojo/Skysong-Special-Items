package org.skysongdev.skysongitems.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Reads, adds and cancels the timers on SkysongItems items: changes stored on an item that are
 * made once their time comes, such as a trait given for 24 hours being removed.
 *
 * <p>Timers run lazily: an item's due timers are applied when a player deals with it (opens
 * an inventory holding it, clicks in one, uses it, or runs a {@code /si} command on it), never
 * on a clock. So an item nobody touched may still carry timers whose time has passed;
 * {@link #list} shows them as they are stored.
 *
 * <p>Adding and cancelling follow the same rules as the other writes: they only accept SkysongItems
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
     * Makes the change once the time has passed, like {@code /si tag set arcrot 3 after 1d}.
     * It replaces a timer on the item that makes the same change to the same tag or trait.
     *
     * @param time how long from now, as staff type it: a number and a unit (s, m, h, d, w, mo,
     *             y), combinable, such as {@code 24h} or {@code 1d12h}
     * @return the timer stored
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  the time isn't one, or the change can't apply to the item
     *                                  (an unknown tag or trait, or a value of the wrong type)
     * @since 1.10.0
     */
    @NotNull ItemTimer after(ItemStack item, TimerChange change, String time);

    /**
     * Makes the change each time the time passes, starting from now, like
     * {@code /si trait add honed every 1mo}. Otherwise the same as {@link #after}.
     *
     * @since 1.10.0
     */
    @NotNull ItemTimer every(ItemStack item, TimerChange change, String time);

    /**
     * Cancels every timer on the item.
     *
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item
     */
    void clear(ItemStack item);
}
