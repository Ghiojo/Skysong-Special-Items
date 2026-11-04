package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.utils.ItemEditor.Result;
import org.skysongdev.skysongitems.utils.ItemFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * The checks every API write shares. Writes go through
 * {@link org.skysongdev.skysongitems.utils.ItemEditor}, the same code the commands use,
 * and turn the editor's refusals into exceptions for the calling plugin.
 */
final class ItemWrites {
    private static final Map<Result, String> REFUSALS = new EnumMap<>(Result.class);

    static {
        REFUSALS.put(Result.NOT_CHARGED, "the item has no charges");
        REFUSALS.put(Result.ADDING_LOCKED, "the item doesn't allow adding charges");
        REFUSALS.put(Result.INVALID_AMOUNT, "the amount is out of range");
        REFUSALS.put(Result.UNKNOWN_TAG, "there is no tag with this id");
        REFUSALS.put(Result.NOT_A_NUMBER, "the tag holds numbers");
        REFUSALS.put(Result.NOT_A_BOOLEAN, "the tag holds true or false");
        REFUSALS.put(Result.UNKNOWN_PROPERTY, "there is no property with this id");
    }

    /** Which items a write accepts. */
    enum ItemCheck {
        /**
         * Only SkysongItems items. Writing to a plain item would skip the version
         * migration {@code /si update} runs, so plugins convert it explicitly with
         * {@code templates().toSkysongItem} first.
         */
        SKYSONG_ITEM,
        /** Any item that isn't null or air, for items nobody holds yet. */
        ANY_ITEM;

        /** Throws unless called on the main thread with an item this check accepts. */
        void require(ItemStack item) {
            if (SKYSONG_ITEM.equals(this)) {
                requireSkysongItem(item);
            } else {
                requireItem(item);
            }
        }
    }

    private ItemWrites() {
        // Static methods only.
    }

    /** Throws unless called on the main thread with a SkysongItems item. */
    static void requireSkysongItem(ItemStack item) {
        requireItem(item);
        if (!ItemFactory.isSkysongItem(item)) {
            throw new IllegalArgumentException("Not a SkysongItems item. Convert it first with templates().toSkysongItem(item).");
        }
    }

    /** Throws unless called on the main thread with an item that isn't null or air. */
    static void requireItem(ItemStack item) {
        StoredValues.requireMainThread();
        if (item == null || item.getType().isAir()) {
            throw new IllegalArgumentException("SkysongItems API writes need an item, not null or air");
        }
    }

    /**
     * Throws when the editor refused the change.
     *
     * @param subject what was written, such as the tag id, for the exception message
     */
    static void requireDone(Result result, String subject) {
        if (!Result.DONE.equals(result)) {
            throw new IllegalArgumentException("Cannot write " + subject + ": " + REFUSALS.getOrDefault(result, result.name()));
        }
    }
}
