package org.skysongdev.skysongitems.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * Reads and writes the HP of SkysongItems items: hit points an item counts down from its
 * max HP, drawn in its lore like charges. An empty result means the item has no HP at
 * all, which is different from an item with 0 HP left. Nothing happens to an item at 0 HP;
 * what that means is up to the plugin reading it.
 *
 * <p>Players can only take HP off an item with {@code /si hp reduce}; restoring it is for
 * staff. Through the API, {@link #add} restores HP as well, so a plugin that only damages
 * items should pass a negative amount.
 *
 * <p>Writes follow the same rules as {@link ItemCharges}: they change the item in place,
 * redraw the lore, fire an {@link org.skysongdev.skysongitems.api.event.ItemTagChangeEvent}
 * for the {@code item_hp} tag, and only accept SkysongItems items, except through
 * {@link SkysongItemsApi#detached()}.
 *
 * <p>Must be called on the server's main thread, and throws {@link IllegalStateException} otherwise.
 *
 * @since 1.6.0
 */
@ApiStatus.NonExtendable
public interface ItemHp {
    /** The HP left on the item, empty when it has none stored. */
    OptionalInt get(@Nullable ItemStack item);

    /** The item's max HP, empty when it has none stored. */
    OptionalInt getMax(@Nullable ItemStack item);

    /** Whether the item has more than 0 HP left. */
    boolean hasLeft(@Nullable ItemStack item);

    /**
     * Sets the item's HP, like {@code /si hp set}: capped at the item's max HP, and an
     * item without HP gets this amount as its max and the item HP trait.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @return the HP the item has now
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  or the amount is negative
     */
    int set(@Nullable Player holder, ItemStack item, int hp);

    /** Same as {@link #set(Player, ItemStack, int)} with no holder. */
    default int set(ItemStack item, int hp) {
        return set(null, item, hp);
    }

    /**
     * Adds HP to the item, or takes it off for a negative amount, like {@code /si hp add}.
     * The result stays between 0 and the item's max HP.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @return the HP the item has now
     * @throws IllegalArgumentException when the item is null, air, not a SkysongItems item
     *                                  or has no HP
     */
    int add(@Nullable Player holder, ItemStack item, int amount);

    /** Same as {@link #add(Player, ItemStack, int)} with no holder. */
    default int add(ItemStack item, int amount) {
        return add(null, item, amount);
    }

    /**
     * Sets the item's max HP, like {@code /si hp setmax}. HP the item already has is kept,
     * and an item without HP gets this amount as its HP.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  or the amount is under 1
     */
    void setMax(@Nullable Player holder, ItemStack item, int maxHp);

    /** Same as {@link #setMax(Player, ItemStack, int)} with no holder. */
    default void setMax(ItemStack item, int maxHp) {
        setMax(null, item, maxHp);
    }
}
