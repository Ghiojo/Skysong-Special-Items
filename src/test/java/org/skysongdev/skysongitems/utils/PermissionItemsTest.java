package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers which permissions are given back when a player joins: item
 * permissions end at logout, so those of the items still held are granted again.
 */
class PermissionItemsTest extends MockBukkitTest {
    private PlayerMock player;

    @BeforeEach
    void addPlayer() {
        player = server.addPlayer();
    }

    @Test
    void emptyInventoryHoldsNoPermissions() {
        assertTrue(PermissionItems.heldPermissions(player).isEmpty());
    }

    @Test
    void itemsWithoutAPermissionAreIgnored() {
        player.getInventory().addItem(new ItemStack(Material.STICK));

        assertTrue(PermissionItems.heldPermissions(player).isEmpty());
    }

    @Test
    void permissionItemsAnywhereInTheInventoryAreFound() {
        player.getInventory().addItem(permissionItem("skysongitems.first"));
        player.getInventory().setItemInOffHand(permissionItem("skysongitems.second"));

        assertEquals(List.of("skysongitems.first", "skysongitems.second"),
                PermissionItems.heldPermissions(player));
    }

    private static ItemStack permissionItem(String permission) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(ItemKeys.PERMISSION, PersistentDataType.STRING, permission);
        item.setItemMeta(meta);
        return item;
    }
}
