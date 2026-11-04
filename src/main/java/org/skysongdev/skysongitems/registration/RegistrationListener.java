package org.skysongdev.skysongitems.registration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.skysongdev.skysongitems.SkysongItems;

/**
 * Asks other plugins for their tags and properties once every plugin has enabled, and
 * drops a plugin's tags and properties as soon as it is disabled.
 */
public final class RegistrationListener implements Listener {
    private final SkysongItems plugin;

    public RegistrationListener(SkysongItems plugin) {
        this.plugin = plugin;
    }

    /**
     * Paper fires this once all plugins have enabled and before players can join, so
     * the plugins that softdepend on SkysongItems are listening by now.
     */
    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        plugin.reloadRegistries();
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin)) {
            return;
        }
        String owner = event.getPlugin().getName();
        plugin.getTagRegistry().removeOwnedBy(owner);
        plugin.getPropertyRegistry().removeOwnedBy(owner);
    }
}
