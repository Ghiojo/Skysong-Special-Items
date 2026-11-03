package org.skysongdev.skysongitems.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Reads the properties of SkysongItems items, the ids {@code /si prop add} takes.
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

    /** Whether SkysongItems has a property with this id, ignoring case. */
    boolean exists(String propertyId);

    /** Every property id, as written in config.yml. Immutable. */
    Set<String> ids();
}
