package org.skysongdev.skysongitems.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Reads the tags SkysongItems stores on items.
 *
 * <p>A {@code tagId} is the name {@code /si tag set} takes, the {@code namespace:} of a
 * tag in config.yml (for example {@code arcrot}) or a built-in one such as {@code charges}.
 * It is matched ignoring case. Every read is empty for a null or air item, an item without
 * the tag, an unknown id, or a tag of another type.
 *
 * <p>Methods that take an {@link ItemStack} must be called on the server's main thread, and throw
 * {@link IllegalStateException} otherwise;
 * the others are safe from any thread.
 */
@ApiStatus.NonExtendable
public interface ItemTags {
    /** The value of an int tag on the item. */
    OptionalInt getInt(@Nullable ItemStack item, String tagId);

    /** The value of a float tag on the item. */
    Optional<Float> getFloat(@Nullable ItemStack item, String tagId);

    /** The value of a bool tag on the item. */
    Optional<Boolean> getBool(@Nullable ItemStack item, String tagId);

    /** The value of a string tag on the item. */
    Optional<String> getString(@Nullable ItemStack item, String tagId);

    /**
     * Adds up an int tag over the items, each value multiplied by its stack's amount, for example a player's inventory.
     * Null, air and items without the tag count as 0.
     *
     * @param onlyWithChargesLeft when true, only items with more than 0 charges count
     */
    int sumInt(Iterable<ItemStack> items, String tagId, boolean onlyWithChargesLeft);

    /** Whether SkysongItems has a tag with this id. */
    boolean exists(String tagId);

    /** The type of the tag with this id, empty when there is no such tag. */
    Optional<TagType> type(String tagId);

    /** Every tag id, lowercased. Immutable. */
    Set<String> ids();
}
