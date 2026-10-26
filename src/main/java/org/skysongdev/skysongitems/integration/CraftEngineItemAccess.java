package org.skysongdev.skysongitems.integration;

import net.momirealms.craftengine.bukkit.item.BukkitItem;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.Set;

/**
 * The calls {@link CraftEngineIntegration} makes on CraftEngine's item manager.
 *
 * <p>CraftEngine's manager classes refer to libraries bundled only inside the
 * CraftEngine plugin jar, so tests cannot load or mock them; they test the
 * integration against a fake of this interface instead.
 */
interface CraftEngineItemAccess {
    Optional<Key> customId(ItemStack item);

    /** The item with its custom id set; CraftEngine may hand back a new stack. */
    ItemStack withCustomId(ItemStack item, Key id);

    /** The item without the top-level custom data entry {@code tag}. */
    ItemStack withoutTag(ItemStack item, String tag);

    boolean isDefined(Key id);

    Set<Key> definedIds();

    /** The access backed by CraftEngine's running item manager. */
    static CraftEngineItemAccess of(BukkitItemManager items) {
        return new CraftEngineItemAccess() {
            @Override
            public Optional<Key> customId(ItemStack item) {
                return items.wrap(item).customId();
            }

            @Override
            public ItemStack withCustomId(ItemStack item, Key id) {
                BukkitItem wrapped = items.wrap(item);
                wrapped.customId(id);
                return wrapped.getBukkitItem();
            }

            @Override
            public ItemStack withoutTag(ItemStack item, String tag) {
                BukkitItem wrapped = items.wrap(item);
                wrapped.removeTag(tag);
                return wrapped.getBukkitItem();
            }

            @Override
            public boolean isDefined(Key id) {
                return items.getItemDefinition(id).isPresent();
            }

            @Override
            public Set<Key> definedIds() {
                return items.loadedItems().keySet();
            }
        };
    }
}
