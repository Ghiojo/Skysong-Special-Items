package org.skysongdev.skysongitems.apiimpl;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.api.SkysongItemsApi;

/**
 * SkysongItems' implementation of its public API, offered to other plugins through
 * Bukkit's {@link ServicesManager}.
 */
public final class SkysongItemsApiImpl implements SkysongItemsApi {
    /** The API version this implementation provides. See {@link SkysongItemsApi#apiVersion()}. */
    public static final String API_VERSION = "1.0.0";

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
