package org.skysongdev.skysongitems.testsupport;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

/** Builds item stacks carrying stored values, the way SkysongItems stores tags. */
public final class TaggedItems {
    private final ItemStack item;
    private final ItemMeta meta;

    private TaggedItems(Material material, int amount) {
        item = new ItemStack(material, amount);
        meta = item.getItemMeta();
    }

    /** Starts a stack of this many stones. */
    public static TaggedItems stones(int amount) {
        return new TaggedItems(Material.STONE, amount);
    }

    /** Stores a value under the key. */
    public <P, C> TaggedItems with(NamespacedKey key, PersistentDataType<P, C> type, C value) {
        meta.getPersistentDataContainer().set(key, type, value);
        return this;
    }

    /** The finished item. */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
