package org.skysongdev.skysongitems.testsupport;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.PermissionItemSync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stands in for LuckPerms in permission item tests: it records every grant and
 * revoke, and tracks which permissions a player would have as a result.
 */
public final class RecordedPermissions {
    private final Set<String> current = new HashSet<>();
    private final List<String> grants = new ArrayList<>();
    private final List<String> revokes = new ArrayList<>();

    /** A sync that grants and revokes through this recorder. */
    public PermissionItemSync newSync() {
        return new PermissionItemSync(
                (player, permission) -> {
                    grants.add(permission);
                    current.add(permission);
                },
                (player, permission) -> {
                    revokes.add(permission);
                    current.remove(permission);
                });
    }

    /** The permissions the player has now, after every grant and revoke so far. */
    public Set<String> current() {
        return Set.copyOf(current);
    }

    /** Every permission granted, in order, repeats included. */
    public List<String> grants() {
        return List.copyOf(grants);
    }

    /** Every permission revoked, in order, repeats included. */
    public List<String> revokes() {
        return List.copyOf(revokes);
    }

    /** A paper item carrying the permission in its permission tag. */
    public static ItemStack permissionItem(String permission) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.PERMISSION, PersistentDataType.STRING, permission);
        item.setItemMeta(meta);
        return item;
    }
}
