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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * The tags an item can carry and the data type of each: the built-in tags the plugin
 * relies on, plus the ones configured in the {@code tags} section of config.yml.
 *
 * <p>Each load builds new maps and swaps them in at once, so lookups from any thread
 * see either the old tags or the new ones, never a half-loaded registry. Tags are
 * also indexed by their lowercased key name, so looking one up by id costs one map
 * lookup however many tags are configured.
 */
public class TagRegistry {
    private volatile Snapshot snapshot = new Snapshot(Collections.emptyMap(), Collections.emptyMap());

    /** The registered tags and their id index, published together. */
    private record Snapshot(Map<NamespacedKey, PersistentDataType<?, ?>> types, Map<String, NamespacedKey> keysById) {
    }

    /**
     * Replaces the registered tags with the built-in ones and the ones in the config.
     * A configured tag with a missing or unknown type is skipped with a warning, since
     * its value could be neither stored nor read.
     */
    public void load(ConfigurationSection config, Logger logger) {
        Map<NamespacedKey, PersistentDataType<?, ?>> loaded = new LinkedHashMap<>();
        Set<String> ids = config.getConfigurationSection("tags").getKeys(false);
        registerBuiltInTags(loaded);
        for (String id : ids) {
            String typeName = config.getString("tags." + id + ".type");
            PersistentDataType<?, ?> type = parseType(typeName);
            if (type == null) {
                logger.warning("Skipping tag " + id + ": unknown type '" + typeName + "' (use int, string, float or bool).");
                continue;
            }
            NamespacedKey key = ItemKeys.named(config.getString("tags." + id + ".namespace"));
            loaded.put(key, type);
        }
        swapIn(loaded);
    }

    /**
     * Publishes the loaded tags. When two tags share a key name in different
     * namespaces, lookups by id find the one registered first, so a built-in tag
     * wins over a configured one.
     */
    private void swapIn(Map<NamespacedKey, PersistentDataType<?, ?>> loaded) {
        Map<String, NamespacedKey> index = new HashMap<>();
        for (NamespacedKey key : loaded.keySet()) {
            index.putIfAbsent(normalise(key.getKey()), key);
        }
        snapshot = new Snapshot(Collections.unmodifiableMap(loaded), Collections.unmodifiableMap(index));
    }

    private static String normalise(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    private static void registerBuiltInTags(Map<NamespacedKey, PersistentDataType<?, ?>> builtIn) {
        builtIn.put(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
        builtIn.put(ItemKeys.CHARGES, PersistentDataType.INTEGER);
        builtIn.put(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN);
        builtIn.put(ItemKeys.VERSION, PersistentDataType.STRING);
        builtIn.put(ItemKeys.LORE, PersistentDataType.STRING);
        builtIn.put(ItemKeys.NAME, PersistentDataType.STRING);
        builtIn.put(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER);
        builtIn.put(ItemKeys.PERMISSION, PersistentDataType.STRING);
        builtIn.put(ItemKeys.FURNITURE_MODEL, PersistentDataType.STRING);
        builtIn.put(ItemKeys.SHIFT_RIGHT_CLICKABLE, PersistentDataType.BOOLEAN);
        builtIn.put(ItemKeys.MATERIAL_ID, PersistentDataType.STRING);
        builtIn.put(ItemKeys.ON_CONSUMPTION, PersistentDataType.STRING);
        builtIn.put(ItemKeys.DYE_COLOR, PersistentDataType.INTEGER);
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
        return snapshot.types().containsKey(key);
    }

    /** The key of the tag matching this id, ignoring case and namespace, or null when there is none. */
    public NamespacedKey getKey(String id) {
        if (id == null) {
            return null;
        }
        return snapshot.keysById().get(normalise(id));
    }

    /**
     * The tag matching this id, ignoring case and namespace, with its data type; null
     * when there is none. Key and type come from the same load, even while a reload runs.
     */
    public RegisteredTag find(String id) {
        if (id == null) {
            return null;
        }
        Snapshot current = snapshot;
        NamespacedKey key = current.keysById().get(normalise(id));
        return key == null ? null : new RegisteredTag(key, current.types().get(key));
    }

    /** Every tag id, the lowercased key name {@link #getKey(String)} finds it by. */
    public Set<String> getIds() {
        return snapshot.keysById().keySet();
    }

    /** A registered tag: the key its value is stored under and the value's data type. */
    public record RegisteredTag(NamespacedKey key, PersistentDataType<?, ?> type) {
    }

    /** The data type of the tag with this key, or null when it is not registered. */
    public PersistentDataType<?, ?> getType(NamespacedKey key) {
        return snapshot.types().get(key);
    }

    /** Every registered tag key. */
    public Set<NamespacedKey> getKeys() {
        return snapshot.types().keySet();
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
        Map<NamespacedKey, PersistentDataType<?, ?>> registered = snapshot.types();
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
            if (registered.containsKey(key)) {
                tags.add(key);
            }
        }
        return tags;
    }
}
