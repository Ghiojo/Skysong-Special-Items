package org.skysongdev.skysongitems.utils;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/** The item properties configured in the {@code properties} section of config.yml. */
public class PropertyRegistry {
    private final List<Property> properties = new ArrayList<>();
    private final List<String> propertyTypes = new ArrayList<>();

    /**
     * Replaces the loaded properties with the ones in the config. Property types are
     * only ever added, so a type removed from the config stays listed until restart.
     */
    public void load(ConfigurationSection config, Logger logger) {
        properties.clear();
        ConfigurationSection section = config.getConfigurationSection("properties");
        if (section == null) {
            logger.warning("No properties section found in the configuration.");
            return;
        }
        for (String id : section.getKeys(false)) {
            logger.warning("Adding property: " + id);
            Property property = createProperty(config, id);
            properties.add(property);
            logger.info("Added " + property.getClass().getSimpleName() + ": " + id);

            if (!propertyTypes.contains(property.getPropType())) {
                propertyTypes.add(property.getPropType());
            }
        }
        logger.info("Total properties added: " + properties.size());
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
        return Collections.unmodifiableList(properties);
    }

    /** Every property type seen since the plugin was enabled. */
    public List<String> getPropertyTypes() {
        return Collections.unmodifiableList(propertyTypes);
    }
}
