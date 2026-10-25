package org.skysongdev.skysongitems.integration;

import dev.jsinco.brewery.bukkit.TheBrewingProject;
import dev.jsinco.brewery.bukkit.api.TheBrewingProjectApi;
import org.bukkit.plugin.Plugin;
import org.skysongdev.skysongitems.integration.listeners.SkysongItemReloader;

/**
 * Everything SkysongItems does with TheBrewingProject: registering Skysong items
 * as brewing ingredients, refreshing item lore when brews are taken out, and
 * exposing the API used to print brew scores.
 *
 * <p>This class references TheBrewingProject's classes, so only create it after
 * {@link SoftDependency#isInstalled} confirms the plugin is present.
 */
public final class BrewingProjectIntegration {
    private final TBPSkysongIntegration itemIntegration = new TBPSkysongIntegration();
    private TheBrewingProjectApi api;

    /** Registers Skysong items with TheBrewingProject. Call from {@code onLoad}. */
    public void registerItemIntegration() {
        itemIntegration.loadIfPossible();
    }

    /** Connects to TheBrewingProject's API and starts listening to its brewing events. */
    public void enable(Plugin plugin) {
        api = TheBrewingProject.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new SkysongItemReloader(), plugin);
    }

    /**
     * Tells TheBrewingProject that Skysong items can now be created. Call once
     * tags and properties are loaded.
     */
    public void markItemsReady() {
        itemIntegration.registerComplete();
    }

    public TheBrewingProjectApi getApi() {
        return api;
    }
}
