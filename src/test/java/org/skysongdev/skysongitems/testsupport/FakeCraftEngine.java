package org.skysongdev.skysongitems.testsupport;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.integration.CraftEngineItemIds;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stands in for CraftEngine, which the main test suite does not have. It keeps
 * the id in a tag of its own and, like CraftEngine, hands back a new stack.
 */
public final class FakeCraftEngine implements CraftEngineItemIds {
    private static final NamespacedKey ID = new NamespacedKey("fakecraftengine", "id");

    private final List<String> loaded = new ArrayList<>();

    /** Makes CraftEngine report an item with this id as loaded. */
    public FakeCraftEngine load(String id) {
        loaded.add(id);
        return this;
    }

    @Override
    public Optional<String> idOf(ItemStack item) {
        return Optional.ofNullable(item.getPersistentDataContainer().get(ID, PersistentDataType.STRING));
    }

    @Override
    public ItemStack withId(ItemStack item, String id) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        meta.getPersistentDataContainer().set(ID, PersistentDataType.STRING, id);
        copy.setItemMeta(meta);
        return copy;
    }

    @Override
    public ItemStack withoutId(ItemStack item) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        meta.getPersistentDataContainer().remove(ID);
        copy.setItemMeta(meta);
        return copy;
    }

    @Override
    public boolean isLoaded(String id) {
        return loaded.contains(id);
    }

    @Override
    public List<String> loadedIds() {
        return loaded.stream().sorted().toList();
    }
}
