package org.skysongdev.skysongitems.utils;

import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
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
     * The permissions carried by the permission items the player holds, one per
     * item: every slot of their inventory, the item on their cursor so an item
     * being moved within the inventory still counts as held, and the items packed
     * inside any bundle or container item (such as a shulker box) among them.
     */
    public static List<String> heldPermissions(Player player) {
        List<String> permissions = new ArrayList<>();
        for (ItemStack item : player.getInventory().getContents()) {
            addPermissions(item, permissions);
        }
        addPermissions(player.getItemOnCursor(), permissions);
        return permissions;
    }

    /** Adds the item's permission, then those of the items packed inside it. */
    private static void addPermissions(ItemStack item, List<String> permissions) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        String permission = meta.getPersistentDataContainer().get(ItemKeys.PERMISSION, PersistentDataType.STRING);
        if (permission != null) {
            permissions.add(permission);
        }
        for (ItemStack packed : packedItems(meta)) {
            addPermissions(packed, permissions);
        }
    }

    /** The items inside a bundle or a container item, empty for any other item. */
    private static List<ItemStack> packedItems(ItemMeta meta) {
        if (meta instanceof BundleMeta bundle) {
            return bundle.getItems();
        }
        if (meta instanceof BlockStateMeta blockMeta && blockMeta.hasBlockState()
                && blockMeta.getBlockState() instanceof Container container) {
            return Arrays.asList(container.getInventory().getContents());
        }
        return List.of();
    }
}
