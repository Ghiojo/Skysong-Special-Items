package org.skysongdev.skysongitems.integration;

import org.bukkit.plugin.PluginManager;

import java.util.logging.Logger;

/**
 * Checks for the optional plugins SkysongItems integrates with.
 *
 * <p>A soft dependency's classes are only on the classpath when that plugin is
 * installed, so any code that touches them must run only after
 * {@link #isInstalled} returns {@code true}. Otherwise the JVM throws
 * {@link NoClassDefFoundError} and SkysongItems fails to load.
 */
public final class SoftDependency {
    public static final String BREWING_PROJECT = "TheBrewingProject";
    public static final String LUCK_PERMS = "LuckPerms";
    public static final String CRAFT_ENGINE = "CraftEngine";

    private SoftDependency() {
    }

    /**
     * Returns whether the named plugin is installed, logging a single warning
     * that names the features turned off when it is not.
     *
     * @param pluginName       the plugin's name as declared in its plugin.yml
     * @param disabledFeatures what SkysongItems will skip without the plugin
     */
    public static boolean isInstalled(PluginManager pluginManager, String pluginName, String disabledFeatures, Logger logger) {
        if (pluginManager.getPlugin(pluginName) != null) {
            return true;
        }
        logger.warning(pluginName + " not found, " + disabledFeatures + " will be disabled.");
        return false;
    }
}
