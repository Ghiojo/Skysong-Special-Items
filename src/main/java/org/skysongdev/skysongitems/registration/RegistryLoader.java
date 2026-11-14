package org.skysongdev.skysongitems.registration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.skysongdev.skysongitems.api.event.SkysongItemsReadyEvent;
import org.skysongdev.skysongitems.api.event.SkysongItemsRegisterEvent;
import org.skysongdev.skysongitems.utils.TraitRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry;

import java.util.logging.Logger;

/**
 * Builds the tag and trait registries. Every load starts from new registries filled
 * from the built-in tags and config.yml; a full load then lets other plugins add theirs.
 * The new registries are only handed over once complete, so nothing ever reads a
 * half-built one.
 */
public final class RegistryLoader {
    /** Puts newly built registries in use. */
    @FunctionalInterface
    public interface RegistriesConsumer {
        void use(TagRegistry tags, TraitRegistry traits);
    }

    private final PluginManager pluginManager;
    private final Logger logger;

    public RegistryLoader(PluginManager pluginManager, Logger logger) {
        this.pluginManager = pluginManager;
        this.logger = logger;
    }

    /**
     * Loads only the built-in tags and config.yml, without asking other plugins. Used
     * while SkysongItems enables, before the plugins that depend on it are listening.
     */
    public void loadConfig(ConfigurationSection config, RegistriesConsumer consumer) {
        TagRegistry tags = loadTags(config);
        consumer.use(tags, loadTraits(config));
    }

    /**
     * Loads the built-in tags and config.yml, fires {@link SkysongItemsRegisterEvent} so
     * other plugins add their tags and traits, puts the result in use, then fires
     * {@link SkysongItemsReadyEvent}. Main thread only, as it fires events.
     */
    public void loadWithPlugins(ConfigurationSection config, RegistriesConsumer consumer) {
        TagRegistry tags = loadTags(config);
        TraitRegistry traits = loadTraits(config);
        pluginManager.callEvent(new SkysongItemsRegisterEvent(new PluginRegistrar(tags, traits, logger)));
        consumer.use(tags, traits);
        pluginManager.callEvent(new SkysongItemsReadyEvent());
    }

    private TagRegistry loadTags(ConfigurationSection config) {
        TagRegistry tags = new TagRegistry();
        tags.load(config, logger);
        return tags;
    }

    private TraitRegistry loadTraits(ConfigurationSection config) {
        TraitRegistry traits = new TraitRegistry();
        traits.load(config, logger);
        return traits;
    }
}
