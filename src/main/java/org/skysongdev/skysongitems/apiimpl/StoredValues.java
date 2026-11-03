package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

/**
 * Reads values stored on items for the API. Reads go through
 * {@link ItemStack#getPersistentDataContainer()}, which looks at the item's data in
 * place, never through {@code getItemMeta()}, which copies it: SkysongStats recounts
 * on every inventory change, so these reads run often with many players online.
 */
final class StoredValues {
    private StoredValues() {
        // Static methods only.
    }

    /**
     * The value stored under the key, or null when the item is null or air, has no
     * value under the key, or has one of another data type.
     */
    static <P, C> C read(ItemStack item, NamespacedKey key, PersistentDataType<P, C> type) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        var data = item.getPersistentDataContainer();
        return data.has(key, type) ? data.get(key, type) : null;
    }

    /**
     * Throws unless called on the server's main thread. Item stacks are not safe to read
     * while the main thread may be changing them.
     */
    static void requireMainThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("SkysongItems API item reads must run on the server's main thread");
        }
    }
}
