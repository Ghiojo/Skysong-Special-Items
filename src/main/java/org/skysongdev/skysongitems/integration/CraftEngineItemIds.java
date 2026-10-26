package org.skysongdev.skysongitems.integration;

import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Reads and writes the CraftEngine item id of an item, the {@code craftengine:id}
 * entry CraftEngine keeps in the item's custom data. CraftEngine treats any item
 * that carries the id of one of its items as that item, so an id that names a piece
 * of furniture makes the item placeable as that furniture.
 *
 * <p>The id lives outside the item's persistent data container, so it cannot be
 * edited like a Skysong tag; writing it leaves the item's Skysong tags untouched.
 * Ids are namespaced, for example {@code skysong:oak_table}.
 */
public interface CraftEngineItemIds {
    /** The item's CraftEngine id, empty when it has none. */
    Optional<String> idOf(ItemStack item);

    /** The item with its CraftEngine id set to {@code id}. Hold on to the returned stack, not the one passed in. */
    ItemStack withId(ItemStack item, String id);

    /** The item without a CraftEngine id. Hold on to the returned stack, not the one passed in. */
    ItemStack withoutId(ItemStack item);

    /** Whether CraftEngine has loaded an item with this id. */
    boolean isLoaded(String id);

    /** The ids of every item CraftEngine has loaded, sorted. */
    List<String> loadedIds();
}
