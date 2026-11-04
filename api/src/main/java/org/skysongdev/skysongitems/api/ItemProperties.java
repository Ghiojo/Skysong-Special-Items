package org.skysongdev.skysongitems.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Reads and writes the properties of SkysongItems items, the ids {@code /si prop add} takes.
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
public interface ItemProperties {
    /** The property ids on the item in the order they were added, empty for none. Immutable. */
    List<String> get(@Nullable ItemStack item);

    /** Whether the item has this property, matching the id exactly. */
    boolean has(@Nullable ItemStack item, String propertyId);

    /**
     * Adds a property to the item, like {@code /si prop add}. A printable property's
     * value tags are set to their empty value (0, false or an empty text).
     *
     * @param holder the player holding the item, named in the change events; may be null
     * @return true when added, false when the item already has it
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  or there is no property with this id
     */
    boolean add(@Nullable Player holder, ItemStack item, String propertyId);

    /** Same as {@link #add(Player, ItemStack, String)} with no holder. */
    default boolean add(ItemStack item, String propertyId) {
        return add(null, item, propertyId);
    }

    /**
     * Takes a property off the item, like {@code /si prop remove}, whether or not the
     * property still exists in SkysongItems.
     *
     * @param holder the player holding the item, named in the change events; may be null
     * @return true when removed, false when the item didn't have it
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item
     */
    boolean remove(@Nullable Player holder, ItemStack item, String propertyId);

    /** Same as {@link #remove(Player, ItemStack, String)} with no holder. */
    default boolean remove(ItemStack item, String propertyId) {
        return remove(null, item, propertyId);
    }

    /** Whether SkysongItems has a property with this id, ignoring case. */
    boolean exists(String propertyId);

    /** Every property id, as written in config.yml. Immutable. */
    Set<String> ids();
}
