package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.api.ItemAccess;
import org.skysongdev.skysongitems.api.ItemCharges;
import org.skysongdev.skysongitems.api.ItemHp;
import org.skysongdev.skysongitems.api.ItemProperties;
import org.skysongdev.skysongitems.api.ItemTimers;
import org.skysongdev.skysongitems.api.ItemTraits;
import org.skysongdev.skysongitems.api.ItemTags;
import org.skysongdev.skysongitems.api.ItemTemplates;
import org.skysongdev.skysongitems.api.ItemTransformations;
import org.skysongdev.skysongitems.api.SkysongItemsApi;
import org.skysongdev.skysongitems.commands.lore.Updater;
import org.skysongdev.skysongitems.transform.ItemTransformer;
import org.skysongdev.skysongitems.utils.ItemEditor;
import org.skysongdev.skysongitems.utils.TraitRegistry;
import org.skysongdev.skysongitems.utils.TagRegistry;

/**
 * SkysongItems' implementation of its public API, offered to other plugins through
 * Bukkit's {@link ServicesManager}.
 */
public final class SkysongItemsApiImpl implements SkysongItemsApi {
    /** The API version this implementation provides. See {@link SkysongItemsApi#apiVersion()}. */
    public static final String API_VERSION = "1.7.0";

    private final ItemAccess held;
    private final ItemAccess detached;
    private final ItemTemplates templates;
    private final ItemTransformations transformations;

    /** An API reading through the plugin's tag and trait registries and writing through its item editor. */
    public SkysongItemsApiImpl(SkysongItems plugin) {
        this(plugin.getTagRegistry(), plugin.getTraitRegistry(), plugin.getItemEditor(), plugin.getUpdater(),
                plugin.getItemTransformer());
    }

    SkysongItemsApiImpl(TagRegistry tagRegistry, TraitRegistry traitRegistry, ItemEditor editor, Updater updater,
                        ItemTransformer transformer) {
        this.held = ItemAccessImpl.held(tagRegistry, traitRegistry, editor);
        this.detached = ItemAccessImpl.detached(tagRegistry, traitRegistry, editor);
        this.templates = new ItemTemplatesImpl(updater);
        this.transformations = new ItemTransformationsImpl(transformer);
    }

    @Override
    public @NotNull ItemTags tags() {
        return held.tags();
    }

    @Override
    public @NotNull ItemCharges charges() {
        return held.charges();
    }

    @Override
    public @NotNull ItemHp itemHp() {
        return held.itemHp();
    }

    @Override
    public @NotNull ItemTimers timers() {
        return held.timers();
    }

    @Override
    public @NotNull ItemTraits traits() {
        return held.traits();
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ItemProperties properties() {
        return held.properties();
    }

    @Override
    public @NotNull ItemAccess detached() {
        return detached;
    }

    @Override
    public @NotNull ItemTemplates templates() {
        return templates;
    }

    @Override
    public @NotNull ItemTransformations transformations() {
        return transformations;
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
