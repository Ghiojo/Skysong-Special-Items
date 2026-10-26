package org.skysongdev.skysongitems.integration;

import net.momirealms.craftengine.bukkit.item.BukkitItem;
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

    private final BukkitItemManager items;

    public CraftEngineIntegration() {
        this(BukkitItemManager.instance());
    }

    CraftEngineIntegration(BukkitItemManager items) {
        this.items = items;
    }

    @Override
    public Optional<String> idOf(ItemStack item) {
        return items.wrap(item).customId().map(Key::asString);
    }

    @Override
    public ItemStack withId(ItemStack item, String id) {
        BukkitItem wrapped = items.wrap(item);
        wrapped.customId(Key.of(id));
        return wrapped.getBukkitItem();
    }

    @Override
    public ItemStack withoutId(ItemStack item) {
        BukkitItem wrapped = items.wrap(item);
        wrapped.removeTag(ID_TAG);
        return wrapped.getBukkitItem();
    }

    @Override
    public boolean isLoaded(String id) {
        return items.getItemDefinition(Key.of(id)).isPresent();
    }

    @Override
    public List<String> loadedIds() {
        return items.loadedItems().keySet().stream()
                .map(Key::asString)
                .sorted()
                .toList();
    }
}
