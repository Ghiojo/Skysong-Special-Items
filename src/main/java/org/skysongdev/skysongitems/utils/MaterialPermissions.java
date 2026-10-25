package org.skysongdev.skysongitems.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permissible;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The permission a player needs to use a SkysongItem, keyed by the item's material id,
 * read from materials.yml. Some plugin integrations use it to stop a player from using
 * an item they should not be able to.
 */
public class MaterialPermissions {
    private static final String FILE_NAME = "materials.yml";
    /** Staff with this permission may use every item, whatever its material permission. */
    static final String STAFF_BYPASS_PERMISSION = "skysongspecialitems.cit";

    private final Map<String, String> permissions = new HashMap<>();

    /**
     * Replaces the loaded permissions with the ones in materials.yml from the data folder,
     * creating the file with example entries when missing.
     */
    public void load(File dataFolder, Logger logger) {
        permissions.clear();
        File materialFile = new File(dataFolder, FILE_NAME);
        if (!materialFile.exists()) {
            createDefaultFile(materialFile, logger);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(materialFile);
        ConfigurationSection materials = yaml.getConfigurationSection("materials");
        if (materials == null) {
            logger.warning("No materials section found in materials.yml.");
            return;
        }

        for (String material : materials.getKeys(false)) {
            String permission = materials.getString(material);
            if (permission != null) {
                permissions.put(material, permission);
                logger.info("Added material permission: " + material + " -> " + permission);
            } else {
                logger.warning("No permission found for material: " + material);
            }
        }
    }

    private static void createDefaultFile(File materialFile, Logger logger) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("materials.IRON_INGOT", "skysongitems.use.iron");
        yaml.set("materials.DIAMOND", "skysongitems.use.diamond");
        try {
            yaml.save(materialFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create materials.yml: " + e.getMessage(), e);
        }
    }

    /** The permission needed to use items with this material id, or null when none is configured. */
    public String getPermission(String materialId) {
        return permissions.get(materialId);
    }

    /**
     * Whether the user may use an item with this material id: true when no permission is
     * configured for it, when the user has that permission, or when the user is staff.
     */
    public boolean canUse(Permissible user, String materialId) {
        String permission = materialId == null ? null : getPermission(materialId);
        return permission == null
                || user.hasPermission(permission)
                || user.hasPermission(STAFF_BYPASS_PERMISSION);
    }
}
