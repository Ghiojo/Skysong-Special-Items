package org.skysongdev.skysongitems.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

/**
 * Reads the charges of SkysongItems items. An empty result means the item is not a
 * charged item at all, which is different from a charged item with 0 charges left.
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
}
