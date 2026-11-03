package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemProperties;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.ItemTemplates;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.utils.PropertyRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry;

/**
 * SkysongItems' implementation of its public API, offered to other plugins through
 * Bukkit's {@link ServicesManager}.
 */
public final class SkysongItemsApiImpl implements SkysongItemsApi {
    /** The API version this implementation provides. See {@link SkysongItemsApi#apiVersion()}. */
    public static final String API_VERSION = "1.0.0";

    private final ItemTags tags;
    private final ItemCharges charges;
    private final ItemProperties properties;
    private final ItemTemplates templates;

    /** An API reading through the plugin's tag and property registries. */
    public SkysongItemsApiImpl(SkysongItems plugin) {
        this(plugin.getTagRegistry(), plugin.getPropertyRegistry());
    }

    SkysongItemsApiImpl(TagRegistry tagRegistry, PropertyRegistry propertyRegistry) {
        this.charges = new ItemChargeReader();
        this.tags = new ItemTagReader(tagRegistry, charges);
        this.properties = new ItemPropertyReader(propertyRegistry);
        this.templates = new ItemTemplateReader();
    }

    @Override
    public @NotNull ItemTags tags() {
        return tags;
    }

    @Override
    public @NotNull ItemCharges charges() {
        return charges;
    }

    @Override
    public @NotNull ItemProperties properties() {
        return properties;
    }

    @Override
    public @NotNull ItemTemplates templates() {
        return templates;
    }

    @Override
    public @NotNull String apiVersion() {
        return API_VERSION;
    }

    /** Offers this API to other plugins, owned by {@code owner}. */
    public void register(ServicesManager services, Plugin owner) {
        services.register(SkysongItemsApi.class, this, owner, ServicePriority.Normal);
    }

    /** Stops offering this API, so {@link SkysongItemsApi#get()} fails until it is registered again. */
    public void unregister(ServicesManager services) {
        services.unregister(SkysongItemsApi.class, this);
    }
}
