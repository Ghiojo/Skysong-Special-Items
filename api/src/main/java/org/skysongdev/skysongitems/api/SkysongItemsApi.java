package org.skysongdev.skysongitems.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point of the SkysongItems API, the supported way for other plugins to work with
 * SkysongItems' items. Classes outside the {@code org.skysongdev.skysongitems.api} package
 * are internal and may change without notice.
 *
 * <p>SkysongItems registers this API with Bukkit's {@link org.bukkit.plugin.ServicesManager}
 * at the end of its {@code onEnable} and removes it in {@code onDisable}. Plugins that use it
 * add {@code softdepend: [Skysong_Items]} (or {@code depend}) to their plugin.yml, so they
 * enable after SkysongItems, and get it with {@link #get()}:
 *
 * <pre>{@code
 * SkysongItemsApi items = SkysongItemsApi.get();
 * }</pre>
 *
 * <p>SkysongItems implements this interface; other plugins must not.
 */
@ApiStatus.NonExtendable
public interface SkysongItemsApi {

    /**
     * The API registered by SkysongItems.
     *
     * @return the API
     * @throws IllegalStateException when SkysongItems is not installed or not enabled yet
     */
    static @NotNull SkysongItemsApi get() {
        RegisteredServiceProvider<SkysongItemsApi> registration =
                Bukkit.getServicesManager().getRegistration(SkysongItemsApi.class);
        if (registration == null) {
            throw new IllegalStateException("The SkysongItems API is not available: SkysongItems is not enabled."
                    + " Add 'softdepend: [Skysong_Items]' to your plugin.yml and check that SkysongItems is installed.");
        }
        return registration.getProvider();
    }

    /**
     * The version of the API that the installed SkysongItems implements, as
     * {@code major.minor.patch}. A plugin can compare it with the version it was built
     * against and log a clear message instead of failing with {@link NoSuchMethodError}.
     * Adding methods raises the minor version; removing or changing one raises the major.
     *
     * @return the API version, for example {@code "1.0.0"}
     */
    @NotNull String apiVersion();
}
