package org.skysongdev.skysongitems.api;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Reads and writes the tags SkysongItems stores on items.
 *
 * <p>A {@code tagId} is the name {@code /si tag set} takes, the {@code namespace:} of a
 * tag in config.yml (for example {@code arcrot}) or a built-in one such as {@code charges}.
 * It is matched ignoring case. Every read is empty for a null or air item, an item without
 * the tag, an unknown id, or a tag of another type.
 *
 * <p>The {@code permission} tag, which grants permissions to whoever holds the item, can be
 * read but never written or removed through the API. Neither can the tags SkysongItems keeps
 * for itself: {@code version}, {@code lore} and {@code name}.
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

    /**
     * Sets a tag on the item, replacing any value it had, like {@code /si tag set}.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @param value  an {@code Integer} for an int tag, any {@code Number} for a float tag,
     *               a {@code Boolean} for a bool tag or a {@code String} for a string tag
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  the tag doesn't exist or can't be written, or the value
     *                                  doesn't fit the tag's type
     */
    void set(@Nullable Player holder, ItemStack item, String tagId, Object value);

    /** Same as {@link #set(Player, ItemStack, String, Object)} with no holder. */
    default void set(ItemStack item, String tagId, Object value) {
        set(null, item, tagId, value);
    }

    /**
     * Takes a tag off the item, like {@code /si tag remove}.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @return whether the item had the tag
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  or the tag doesn't exist or can't be written
     */
    boolean remove(@Nullable Player holder, ItemStack item, String tagId);

    /** Same as {@link #remove(Player, ItemStack, String)} with no holder. */
    default boolean remove(ItemStack item, String tagId) {
        return remove(null, item, tagId);
    }

    /** Whether SkysongItems has a tag with this id. */
    boolean exists(String tagId);

    /** The type of the tag with this id, empty when there is no such tag. */
    Optional<TagType> type(String tagId);

    /** Every tag id, lowercased. Immutable. */
    Set<String> ids();
}
