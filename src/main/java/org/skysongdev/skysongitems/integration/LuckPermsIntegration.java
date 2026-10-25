package org.skysongdev.skysongitems.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

import java.util.UUID;

/**
 * Grants and revokes the permissions carried by permission items, through LuckPerms.
 *
 * <p>The permissions are granted as transient nodes, which LuckPerms keeps in
 * memory only: they end when the player logs out or the server stops, so a
 * player never keeps an item's permission after the item is gone.
 *
 * <p>This class references LuckPerms' classes, so only create it after
 * {@link SoftDependency#isInstalled} confirms the plugin is present.
 */
public final class LuckPermsIntegration {
    private final LuckPerms luckPerms;

    public LuckPermsIntegration() {
        this(LuckPermsProvider.get());
    }

    LuckPermsIntegration(LuckPerms luckPerms) {
        this.luckPerms = luckPerms;
    }

    /** Gives the online player the permission until they log out. Does nothing if they are offline. */
    public void grant(UUID playerId, String permission) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user != null) {
            user.transientData().add(node(permission));
        }
    }

    /**
     * Takes the permission away from the online player. Does nothing if they are offline.
     *
     * <p>Permission items granted saved nodes before they granted transient ones,
     * so a saved node for the permission is removed and the user saved as well.
     */
    public void revoke(UUID playerId, String permission) {
        User user = luckPerms.getUserManager().getUser(playerId);
        if (user == null) {
            return;
        }
        Node node = node(permission);
        user.transientData().remove(node);
        if (user.data().remove(node).wasSuccessful()) {
            luckPerms.getUserManager().saveUser(user);
        }
    }

    private Node node(String permission) {
        return luckPerms.getNodeBuilderRegistry().forKey(permission).build();
    }
}
