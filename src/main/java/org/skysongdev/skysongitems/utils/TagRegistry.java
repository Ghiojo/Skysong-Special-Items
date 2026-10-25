package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The tags an item can carry and the data type of each: the built-in tags the plugin
 * relies on, plus the ones configured in the {@code tags} section of config.yml.
 */
public class TagRegistry {
    private final Map<NamespacedKey, PersistentDataType<?, ?>> types = new HashMap<>();

    /**
     * Replaces the registered tags with the built-in ones and the ones in the config.
     * A configured tag with a missing or unknown type is skipped with a warning, since
     * its value could be neither stored nor read.
     */
    public void load(ConfigurationSection config, Logger logger) {
        types.clear();
        Set<String> ids = config.getConfigurationSection("tags").getKeys(false);
        registerBuiltInTags();
        for (String id : ids) {
            String typeName = config.getString("tags." + id + ".type");
            PersistentDataType<?, ?> type = parseType(typeName);
            if (type == null) {
                logger.warning("Skipping tag " + id + ": unknown type '" + typeName + "' (use int, string, float or bool).");
                continue;
            }
            NamespacedKey key = ItemKeys.named(config.getString("tags." + id + ".namespace"));
            types.put(key, type);
        }
    }

    private void registerBuiltInTags() {
        types.put(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
        types.put(ItemKeys.CHARGES, PersistentDataType.INTEGER);
        types.put(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN);
        types.put(ItemKeys.VERSION, PersistentDataType.STRING);
        types.put(ItemKeys.LORE, PersistentDataType.STRING);
        types.put(ItemKeys.NAME, PersistentDataType.STRING);
        types.put(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER);
        types.put(ItemKeys.PERMISSION, PersistentDataType.STRING);
        types.put(ItemKeys.FURNITURE_MODEL, PersistentDataType.STRING);
        types.put(ItemKeys.SHIFT_RIGHT_CLICKABLE, PersistentDataType.BOOLEAN);
        types.put(ItemKeys.MATERIAL_ID, PersistentDataType.STRING);
        types.put(ItemKeys.ON_CONSUMPTION, PersistentDataType.STRING);
        types.put(ItemKeys.DYE_COLOR, PersistentDataType.INTEGER);
    }

    /** The data type for a config type name; a missing or unknown name gives null. */
    private static PersistentDataType<?, ?> parseType(String typeName) {
        if (typeName == null) {
            return null;
        }
        return switch (typeName.toLowerCase()) {
            case "int" -> PersistentDataType.INTEGER;
            case "string" -> PersistentDataType.STRING;
            case "float" -> PersistentDataType.FLOAT;
            case "bool" -> PersistentDataType.BOOLEAN;
            default -> null;
        };
    }

    /** Whether a tag's key matches this id, ignoring case and namespace. */
    public boolean contains(String id) {
        return getKey(id) != null;
    }

    /** Whether this exact key is a registered tag. */
    public boolean contains(NamespacedKey key) {
        return types.containsKey(key);
    }

    /** The key of the tag matching this id, ignoring case and namespace, or null when there is none. */
    public NamespacedKey getKey(String id) {
        for (NamespacedKey key : types.keySet()) {
            if (key.getKey().equalsIgnoreCase(id)) {
                return key;
            }
        }
        return null;
    }

    /** The data type of the tag with this key, or null when it is not registered. */
    public PersistentDataType<?, ?> getType(NamespacedKey key) {
        return types.get(key);
    }

    /** Every registered tag key. */
    public Set<NamespacedKey> getKeys() {
        return Collections.unmodifiableSet(types.keySet());
    }

    /** The registered tags present on the item; empty for no item or air. */
    public List<NamespacedKey> getTagsIn(ItemStack item) {
        List<NamespacedKey> tags = new ArrayList<>();
        if (item == null || item.getType() == Material.AIR) {
            return tags;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return tags;
        }
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
            if (types.containsKey(key)) {
                tags.add(key);
            }
        }
        return tags;
    }
}
