package org.skysongdev.skysongitems.utils;

import org.bukkit.entity.Player;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * Grants and revokes the permission a permission item carries in its
 * {@link ItemKeys#PERMISSION} tag. Both do nothing when LuckPerms is not installed.
 */
public final class PermissionItems {
    private PermissionItems() {
    }

    public static void grant(Player player, String permission) {
        update(player, true, permission);
    }

    public static void revoke(Player player, String permission) {
        update(player, false, permission);
    }

    private static void update(Player player, boolean add, String permission) {
        getPlugin().getLuckPerms()
                .ifPresent(luckPerms -> luckPerms.updatePermission(player.getUniqueId(), add, permission));
    }
}
