package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.ItemProperties;
import org.skysongdev.skysongitems.api.event.ItemChangeCause;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.PropertyRegistry;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Reads the properties stored on items and the ones configured in the plugin, and adds
 * or removes them through the {@link ItemEditor}.
 */
final class ItemPropertiesImpl implements ItemProperties {
    private final PropertyRegistry registry;
    private final ItemEditor editor;

    ItemPropertiesImpl(PropertyRegistry registry, ItemEditor editor) {
        this.registry = registry;
        this.editor = editor;
    }

    @Override
    public boolean add(Player holder, ItemStack item, String propertyId) {
        ItemWrites.requireSkysongItem(item);
        ItemEditor.Result result = editorFor(holder).addProperty(item, propertyId);
        if (ItemEditor.Result.ALREADY_PRESENT.equals(result)) {
            return false;
        }
        ItemWrites.requireDone(result, "property " + propertyId);
        return true;
    }

    @Override
    public boolean remove(Player holder, ItemStack item, String propertyId) {
        ItemWrites.requireSkysongItem(item);
        return ItemEditor.Result.DONE.equals(editorFor(holder).removeProperty(item, propertyId));
    }

    private ItemEditor editorFor(Player holder) {
        return editor.editedBy(holder, ItemChangeCause.API);
    }

    @Override
    public List<String> get(ItemStack item) {
        StoredValues.requireMainThread();
        String stored = StoredValues.read(item, ItemKeys.PROPERTIES, PersistentDataType.STRING);
        return List.copyOf(ItemData.parseProperties(stored));
    }

    @Override
    public boolean has(ItemStack item, String propertyId) {
        return get(item).contains(propertyId);
    }

    @Override
    public boolean exists(String propertyId) {
        return registry.contains(propertyId);
    }

    @Override
    public Set<String> ids() {
        Set<String> ids = new LinkedHashSet<>();
        for (Property property : registry.getAll()) {
            ids.add(property.getId());
        }
        return Collections.unmodifiableSet(ids);
    }
}
