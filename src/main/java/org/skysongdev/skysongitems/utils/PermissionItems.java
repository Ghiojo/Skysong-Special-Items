package org.skysongdev.skysongitems.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * Grants and revokes the permission a permission item carries in its
 * {@link ItemKeys#PERMISSION} tag. Granting and revoking do nothing when LuckPerms
 * is not installed.
 */
public final class PermissionItems {
    private PermissionItems() {
    }

    public static void grant(Player player, String permission) {
        getPlugin().getLuckPerms()
                .ifPresent(luckPerms -> luckPerms.grant(player.getUniqueId(), permission));
    }

    public static void revoke(Player player, String permission) {
        getPlugin().getLuckPerms()
                .ifPresent(luckPerms -> luckPerms.revoke(player.getUniqueId(), permission));
    }

    /**
     * Grants the permissions of the permission items in the player's inventory.
     * The permissions end when the player logs out, so this restores them when
     * the player joins.
     */
    public static void grantHeldItems(Player player) {
        heldPermissions(player).forEach(permission -> grant(player, permission));
    }

    /** The permissions carried by the permission items in the player's inventory, one per item. */
    public static List<String> heldPermissions(Player player) {
        List<String> permissions = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                String permission = item.getItemMeta().getPersistentDataContainer()
                        .get(ItemKeys.PERMISSION, PersistentDataType.STRING);
                if (permission != null) {
                    permissions.add(permission);
                }
            }
        }
        return permissions;
    }
}
