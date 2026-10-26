package org.skysongdev.skysongitems.integration;

import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Edits items' CraftEngine ids through CraftEngine's own item API, which writes
 * the {@code craftengine:id} entry next to the item's other custom data, so the
 * Skysong tags in its persistent data container are kept.
 *
 * <p>This class references CraftEngine's classes, so only create it after
 * {@link SoftDependency#isInstalled} confirms the plugin is present.
 */
public final class CraftEngineIntegration implements CraftEngineItemIds {
    /** The custom data entry holding the id, at the top level of the item's custom data. */
    static final String ID_TAG = "craftengine:id";

    private final CraftEngineItemAccess items;

    public CraftEngineIntegration() {
        this(CraftEngineItemAccess.of(BukkitItemManager.instance()));
    }

    CraftEngineIntegration(CraftEngineItemAccess items) {
        this.items = items;
    }

    @Override
    public Optional<String> idOf(ItemStack item) {
        return items.customId(item).map(Key::asString);
    }

    @Override
    public ItemStack withId(ItemStack item, String id) {
        return items.withCustomId(item, Key.of(id));
    }

    @Override
    public ItemStack withoutId(ItemStack item) {
        return items.withoutTag(item, ID_TAG);
    }

    @Override
    public boolean isLoaded(String id) {
        return items.isDefined(Key.of(id));
    }

    @Override
    public List<String> loadedIds() {
        return items.definedIds().stream()
                .map(Key::asString)
                .sorted()
                .toList();
    }
}
