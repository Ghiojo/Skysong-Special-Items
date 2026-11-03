package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.api.ItemProperties;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.PropertyRegistry;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Reads the properties stored on items and the ones configured in the plugin. */
final class ItemPropertyReader implements ItemProperties {
    private final PropertyRegistry registry;

    ItemPropertyReader(PropertyRegistry registry) {
        this.registry = registry;
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
