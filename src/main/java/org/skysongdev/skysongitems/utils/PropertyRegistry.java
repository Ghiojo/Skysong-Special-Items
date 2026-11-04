package org.skysongdev.skysongitems.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The item properties configured in the {@code properties} section of config.yml, and
 * the ones other plugins register through the API.
 *
 * <p>Each load builds new lists and swaps them in at once, so lookups from any thread
 * see either the old properties or the new ones, never a half-loaded registry.
 */
public class PropertyRegistry {
    private volatile Snapshot snapshot = new Snapshot(List.of(), List.of(), Map.of());

    /**
     * The loaded properties and the property types they use, published together, with
     * the plugin that registered each property by lowercase id (config.yml ones have none).
     */
    private record Snapshot(List<Property> properties, List<String> propertyTypes, Map<String, String> owners) {
    }

    /** Replaces the loaded properties, and the property types they use, with the ones in the config. */
    public void load(ConfigurationSection config, Logger logger) {
        List<Property> loaded = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("properties");
        if (section == null) {
            logger.warning("No properties section found in the configuration.");
            publish(loaded, new HashMap<>());
            return;
        }
        for (String id : section.getKeys(false)) {
            logger.warning("Adding property: " + id);
            Property property = createProperty(config, id);
            loaded.add(property);
            logger.info("Added " + property.getClass().getSimpleName() + ": " + id);
        }
        publish(loaded, new HashMap<>());
        logger.info("Total properties added: " + loaded.size());
    }

    /** Swaps in these properties, with the property types they use in order, in one step. */
    private void publish(List<Property> loaded, Map<String, String> owners) {
        List<String> types = new ArrayList<>();
        for (Property property : loaded) {
            if (!types.contains(property.getPropType())) {
                types.add(property.getPropType());
            }
        }
        snapshot = new Snapshot(Collections.unmodifiableList(loaded), Collections.unmodifiableList(types),
                Collections.unmodifiableMap(owners));
    }

    /**
     * Adds a property registered by another plugin, after the configured ones. The caller
     * checks no property with the same id is loaded first.
     */
    public void register(Property property, String owner) {
        Snapshot current = snapshot;
        List<Property> properties = new ArrayList<>(current.properties());
        Map<String, String> owners = new HashMap<>(current.owners());
        properties.add(property);
        owners.put(normalise(property.getId()), owner);
        publish(properties, owners);
    }

    /** Removes every property the named plugin registered, and the property types only they used. */
    public void removeOwnedBy(String owner) {
        Snapshot current = snapshot;
        if (!current.owners().containsValue(owner)) {
            return;
        }
        List<Property> properties = new ArrayList<>(current.properties());
        Map<String, String> owners = new HashMap<>(current.owners());
        properties.removeIf(property -> owner.equals(owners.get(normalise(property.getId()))));
        owners.values().removeIf(owner::equals);
        publish(properties, owners);
    }

    /**
     * Puts the other registry's properties in use here in one step, so code holding this
     * registry sees a load built elsewhere without ever seeing it half-built.
     */
    public void replaceWith(PropertyRegistry other) {
        snapshot = other.snapshot;
    }

    /** The name of the plugin that registered the property, or null for a configured or unknown one. */
    public String ownerOf(String id) {
        return snapshot.owners().get(normalise(id));
    }

    private static String normalise(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    /** Builds the property of the class named by its {@code type}; unknown types give a plain property. */
    private static Property createProperty(ConfigurationSection config, String id) {
        String path = "properties." + id + ".";
        String type = config.getString(path + "type");
        String propType = config.getString(path + "proptype");
        String item = config.getString(path + "item");
        String name = config.getString(path + "name");
        String description = config.getString(path + "description");
        String printable = config.getString(path + "printable");
        ArrayList<String> valueTags = new ArrayList<>(config.getStringList(path + "valuetags"));
        int weight = config.getInt(path + "weight");

        return switch (type.toLowerCase()) {
            case "rename" -> new RenameProperty(id, propType, item, name, description, config.getString(path + "tag"));
            case "printable" -> new PrintableProperty(id, propType, item, name, description, printable, valueTags, weight);
            case "breweryprintable" -> new BreweryPrintableProperty(id, propType, item, name, description, printable, valueTags, weight);
            default -> new Property(id, propType, item, name, description);
        };
    }

    /** Whether a property with this id is loaded, ignoring case. */
    public boolean contains(String id) {
        return get(id) != null;
    }

    /** The property with this id, ignoring case, or null when there is none. */
    public Property get(String id) {
        for (Property property : snapshot.properties()) {
            if (property.getId().equalsIgnoreCase(id)) {
                return property;
            }
        }
        return null;
    }

    /** Every loaded property, in config order. */
    public List<Property> getAll() {
        return snapshot.properties();
    }

    /** Every property type used by a loaded property, in config order. */
    public List<String> getPropertyTypes() {
        return snapshot.propertyTypes();
    }
}
