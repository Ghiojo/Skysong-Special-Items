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
 * <p>The one tag that can be read but never written or removed is {@code version}: it says
 * which of SkysongItems' migration steps the item has been through, so writing it would make
 * the item lie about its own shape. Use {@link ItemTemplates#toSkysongItem} to bring an item
 * up to date instead.
 *
 * <p>Every other tag can be written, {@code permission} among them: an item that grants a
 * permission is something a plugin is meant to be able to make. Writing {@code name} or
 * {@code lore} stores the text SkysongItems draws the item's name and physical description
 * from; as with {@code /si tag set}, what the item shows follows the next time its lore is
 * drawn, for example after a charge or trait change or a {@code /si update}.
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
     * Sets a tag on the item, replacing any value it had, like {@code /si tag set}. The tag
     * stays for good: a removal set up by {@link #setFor} is cancelled.
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
     * Sets a tag on the item for a while, like {@code /si tag set <tag> <value> <time>}: the tag
     * is removed once the time has passed, not restored to an earlier value. Setting the same tag
     * again for a while restarts its time; setting it with {@link #set(Player, ItemStack, String, Object)}
     * keeps it for good.
     *
     * @param time how long, as staff type it: a number and a unit (s, m, h, d, w, mo, y),
     *             combinable, such as {@code 24h} or {@code 1d12h}
     * @throws IllegalArgumentException as {@link #set(Player, ItemStack, String, Object)} does,
     *                                  or when the time isn't one
     * @see ItemTimers
     * @since 1.9.0
     */
    void setFor(@Nullable Player holder, ItemStack item, String tagId, Object value, String time);

    /** Same as {@link #setFor(Player, ItemStack, String, Object, String)} with no holder. */
    default void setFor(ItemStack item, String tagId, Object value, String time) {
        setFor(null, item, tagId, value, time);
    }

    /**
     * Adds an amount to a number tag on the item, like {@code /si tag add}; a negative amount
     * takes it off, like {@code /si tag reduce}. A tag the item doesn't have counts as 0, and
     * the tag never goes below 0.
     *
     * @param holder the player holding the item, named in the change event; may be null
     * @param amount an {@code Integer} for an int tag, any {@code Number} for a float tag
     * @throws IllegalArgumentException when the item is null, air or not a SkysongItems item,
     *                                  the tag doesn't exist, can't be written or doesn't hold
     *                                  numbers, or the amount doesn't fit the tag's type
     * @since 1.8.0
     */
    void add(@Nullable Player holder, ItemStack item, String tagId, Number amount);

    /** Same as {@link #add(Player, ItemStack, String, Number)} with no holder. */
    default void add(ItemStack item, String tagId, Number amount) {
        add(null, item, tagId, amount);
    }

    /**
     * Adds an amount to a number tag for a while, like {@code /si tag add <tag> <amount> <time>}:
     * the same amount is taken back off once the time has passed (a negative amount is given
     * back), still never below 0. These stack: each call undoes only its own amount.
     *
     * @param time how long, as staff type it, such as {@code 24h} or {@code 1d12h}
     * @throws IllegalArgumentException as {@link #add(Player, ItemStack, String, Number)} does,
     *                                  or when the time isn't one
     * @see ItemTimers
     * @since 1.15.0
     */
    void addFor(@Nullable Player holder, ItemStack item, String tagId, Number amount, String time);

    /** Same as {@link #addFor(Player, ItemStack, String, Number, String)} with no holder. */
    default void addFor(ItemStack item, String tagId, Number amount, String time) {
        addFor(null, item, tagId, amount, time);
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
