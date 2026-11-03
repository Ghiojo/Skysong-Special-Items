package org.skysongdev.skysongitems.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * The item properties configured in the {@code properties} section of config.yml.
 *
 * <p>Each load builds new lists and swaps them in at once, so lookups from any thread
 * see either the old properties or the new ones, never a half-loaded registry.
 */
public class PropertyRegistry {
    private volatile List<Property> properties = List.of();
    private volatile List<String> propertyTypes = List.of();

    /** Replaces the loaded properties, and the property types they use, with the ones in the config. */
    public void load(ConfigurationSection config, Logger logger) {
        List<Property> loaded = new ArrayList<>();
        List<String> loadedTypes = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("properties");
        if (section == null) {
            logger.warning("No properties section found in the configuration.");
            publish(loaded, loadedTypes);
            return;
        }
        for (String id : section.getKeys(false)) {
            logger.warning("Adding property: " + id);
            Property property = createProperty(config, id);
            loaded.add(property);
            logger.info("Added " + property.getClass().getSimpleName() + ": " + id);

            if (!loadedTypes.contains(property.getPropType())) {
                loadedTypes.add(property.getPropType());
            }
        }
        publish(loaded, loadedTypes);
        logger.info("Total properties added: " + loaded.size());
    }

    /** Swaps in a load's results. Types are published first, so a reader that sees a property also sees its type. */
    private void publish(List<Property> loaded, List<String> loadedTypes) {
        propertyTypes = Collections.unmodifiableList(loadedTypes);
        properties = Collections.unmodifiableList(loaded);
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
        for (Property property : properties) {
            if (property.getId().equalsIgnoreCase(id)) {
                return property;
            }
        }
        return null;
    }

    /** Every loaded property, in config order. */
    public List<Property> getAll() {
        return properties;
    }

    /** Every property type used by a loaded property, in config order. */
    public List<String> getPropertyTypes() {
        return propertyTypes;
    }
}
