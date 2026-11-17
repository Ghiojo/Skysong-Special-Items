package org.skysongdev.skysongitems.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Reads and writes the traits of SkysongItems items, the ids {@code /si trait add} takes.
 *
 * <p>Writes change the item in place, go through the same checks as the {@code /si}
 * commands, redraw the lore when the change shows in it, and fire the change events in
 * {@code org.skysongdev.skysongitems.api.event}. They only accept SkysongItems items: convert
 * a plain item first with {@link ItemTemplates#toSkysongItem}. The overloads that take a
 * {@code holder} name that player in the events.
 *
 * <p>This is the view for items a player holds. The same methods reached through
 * {@link SkysongItemsApi#detached()} store the value and nothing else, for an item
 * nobody holds yet.
 *
 * <p>Methods that take an {@link ItemStack} must be called on the server's main thread, and throw
 * {@link IllegalStateException} otherwise;
 * the others are safe from any thread.
 */
@ApiStatus.NonExtendable
public interface ItemTraits {
    /** The trait ids on the item in the order they were added, empty for none. Immutable. */
    List<String> get(@Nullable ItemStack item);

    /** Whether the item has this trait, matching the id exactly. */
    boolean has(@Nullable ItemStack item, String traitId);

    /**
     * Adds a trait to the item, like {@code /si trait add}. A printable trait's
     * value tags are set to their empty value (0, false or an empty text). The trait stays
     * for good: a removal set up by {@link #addFor} is cancelled.
     *
     * @param holder the player holding the item, named in the change events; may be null
     * @return true when added, false when the item already has it
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  or there is no trait with this id
     */
    boolean add(@Nullable Player holder, ItemStack item, String traitId);

    /** Same as {@link #add(Player, ItemStack, String)} with no holder. */
    default boolean add(ItemStack item, String traitId) {
        return add(null, item, traitId);
    }

    /**
     * Adds a trait to the item for a while, like {@code /si trait add <trait> <time>}: it is
     * removed once the time has passed. On an item that already has the trait, its time
     * restarts; adding it with {@link #add(Player, ItemStack, String)} keeps it for good.
     *
     * @param time how long, as staff type it: a number and a unit (s, m, h, d, w, mo, y),
     *             combinable, such as {@code 24h} or {@code 1d12h}
     * @return true when added, false when the item already had it
     * @throws IllegalArgumentException as {@link #add(Player, ItemStack, String)} does, or when
     *                                  the time isn't one
     * @see ItemTimers
     * @since 1.9.0
     */
    boolean addFor(@Nullable Player holder, ItemStack item, String traitId, String time);

    /** Same as {@link #addFor(Player, ItemStack, String, String)} with no holder. */
    default boolean addFor(ItemStack item, String traitId, String time) {
        return addFor(null, item, traitId, time);
    }

    /**
     * Takes a trait off the item, like {@code /si trait remove}, whether or not the
     * trait still exists in SkysongItems.
     *
     * @param holder the player holding the item, named in the change events; may be null
     * @return true when removed, false when the item didn't have it
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item
     */
    boolean remove(@Nullable Player holder, ItemStack item, String traitId);

    /** Same as {@link #remove(Player, ItemStack, String)} with no holder. */
    default boolean remove(ItemStack item, String traitId) {
        return remove(null, item, traitId);
    }

    /** Whether SkysongItems has a trait with this id, ignoring case. */
    boolean exists(String traitId);

    /** Every trait id, as written in config.yml. Immutable. */
    Set<String> ids();
}
