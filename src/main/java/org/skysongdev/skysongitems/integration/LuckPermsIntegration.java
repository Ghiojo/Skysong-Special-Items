package org.skysongdev.skysongitems.integration;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;

import java.util.UUID;

/**
 * Grants and revokes the permissions carried by permission items, through LuckPerms.
 *
 * <p>This class references LuckPerms' classes, so only create it after
 * {@link SoftDependency#isInstalled} confirms the plugin is present.
 */
public final class LuckPermsIntegration {
    private final LuckPerms luckPerms = LuckPermsProvider.get();

    /** Adds the permission to the player when {@code add} is true, removes it otherwise. */
    public void updatePermission(UUID playerId, boolean add, String permission) {
        luckPerms.getUserManager().loadUser(playerId).thenAccept( user -> {
            if(add){
                user.data().add(Node.builder(permission).value(add).build());
            } else {
                user.data().remove(Node.builder(permission).build());
            }
            luckPerms.getUserManager().saveUser(user);
        } );
    }
}
