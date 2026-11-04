package org.skysongdev.skysongitems.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * Reads and writes the charges of SkysongItems items. An empty result means the item is not a
 * charged item at all, which is different from a charged item with 0 charges left.
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
 * <p>Must be called on the server's main thread, and throws {@link IllegalStateException} otherwise.
 */
@ApiStatus.NonExtendable
public interface ItemCharges {
    /** The charges left on the item, empty when it has none stored. */
    OptionalInt get(@Nullable ItemStack item);

    /** The item's maximum charges, empty when it has none stored. */
    OptionalInt getMax(@Nullable ItemStack item);

    /** Whether the item has more than 0 charges left. */
    boolean hasLeft(@Nullable ItemStack item);

    /**
     * Sets the item's charges, like {@code /si charges set}: capped at the item's max
     * charges, and an item without charges gets this amount as its max and the charges property.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @return the charges the item has now
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  or the amount is negative
     */
    int set(@Nullable Player holder, ItemStack item, int charges);

    /** Same as {@link #set(Player, ItemStack, int)} with no holder. */
    default int set(ItemStack item, int charges) {
        return set(null, item, charges);
    }

    /**
     * Adds charges to the item, or takes them off for a negative amount, like
     * {@code /si charges add}. The result stays between 0 and the item's max charges.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @return the charges the item has now
     * @throws IllegalArgumentException when the item is null, air, not a SkysongItems item
     *                                  or has no charges, or when adding to an item that
     *                                  doesn't allow adding charges
     */
    int add(@Nullable Player holder, ItemStack item, int amount);

    /** Same as {@link #add(Player, ItemStack, int)} with no holder. */
    default int add(ItemStack item, int amount) {
        return add(null, item, amount);
    }

    /**
     * Sets the item's max charges, like {@code /si charges max}. Charges the item already
     * has are kept, and an item without charges gets this amount as its charges.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  or the amount is under 1
     */
    void setMax(@Nullable Player holder, ItemStack item, int maxCharges);

    /** Same as {@link #setMax(Player, ItemStack, int)} with no holder. */
    default void setMax(ItemStack item, int maxCharges) {
        setMax(null, item, maxCharges);
    }
}
