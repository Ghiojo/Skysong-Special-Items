package org.skysongdev.skysongitems.utils;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedPermissions;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skysongdev.skysongitems.testsupport.RecordedPermissions.permissionItem;

/**
 * Covers the rule permission items follow: a player has an item's permission
 * while the item is in their inventory, and only then, however the item got
 * there or left.
 */
class PermissionItemSyncTest extends MockBukkitTest {
    private static final String PERMISSION = "skysongitems.test";

    private RecordedPermissions permissions;
    private PermissionItemSync sync;
    private PlayerMock player;

    @BeforeEach
    void createSync() {
        permissions = new RecordedPermissions();
        sync = permissions.newSync();
        player = server.addPlayer();
    }

    @Test
    void grantsThePermissionOfAHeldItem() {
        player.getInventory().addItem(permissionItem(PERMISSION));

        sync.sync(player);

        assertEquals(Set.of(PERMISSION), permissions.current());
        assertEquals(Set.of(PERMISSION), sync.grantedTo(player));
    }

    @Test
    void grantsOnlyOnceWhileTheItemStays() {
        player.getInventory().addItem(permissionItem(PERMISSION));

        sync.sync(player);
        sync.sync(player);

        assertEquals(List.of(PERMISSION), permissions.grants());
    }

    @Test
    void revokesOnceTheItemIsGoneHoweverItLeft() {
        player.getInventory().addItem(permissionItem(PERMISSION));
        sync.sync(player);

        // As /clear, a death or another plugin would, without any event this plugin listens to.
        player.getInventory().clear();
        sync.sync(player);

        assertTrue(permissions.current().isEmpty());
        assertTrue(sync.grantedTo(player).isEmpty());
    }

    @Test
    void keepsThePermissionWhileAnotherItemStillCarriesIt() {
        player.getInventory().setItem(0, permissionItem(PERMISSION));
        player.getInventory().setItem(1, permissionItem(PERMISSION));
        sync.sync(player);

        player.getInventory().setItem(0, null);
        sync.sync(player);

        assertEquals(Set.of(PERMISSION), permissions.current());
        assertTrue(permissions.revokes().isEmpty());
    }

    @Test
    void countsArmorTheOffHandAndTheCursor() {
        player.getInventory().setHelmet(permissionItem("skysongitems.helmet"));
        player.getInventory().setItemInOffHand(permissionItem("skysongitems.offhand"));
        player.setItemOnCursor(permissionItem("skysongitems.cursor"));

        sync.sync(player);

        assertEquals(Set.of("skysongitems.helmet", "skysongitems.offhand", "skysongitems.cursor"),
                permissions.current());
    }

    @Test
    void countsItemsPackedInABundle() {
        ItemStack bundle = new ItemStack(Material.BUNDLE);
        BundleMeta meta = (BundleMeta) bundle.getItemMeta();
        meta.addItem(permissionItem(PERMISSION));
        bundle.setItemMeta(meta);
        player.getInventory().addItem(bundle);

        sync.sync(player);

        assertEquals(Set.of(PERMISSION), permissions.current());
    }

    @Test
    void countsItemsPackedInAShulkerBox() {
        ItemStack shulkerBox = new ItemStack(Material.SHULKER_BOX);
        BlockStateMeta meta = (BlockStateMeta) shulkerBox.getItemMeta();
        ShulkerBox state = (ShulkerBox) meta.getBlockState();
        state.getInventory().addItem(permissionItem(PERMISSION));
        meta.setBlockState(state);
        shulkerBox.setItemMeta(meta);
        player.getInventory().addItem(shulkerBox);

        sync.sync(player);

        assertEquals(Set.of(PERMISSION), permissions.current());
    }

    @Test
    void revokesOnceTheBundleHoldingTheItemIsGone() {
        ItemStack bundle = new ItemStack(Material.BUNDLE);
        BundleMeta meta = (BundleMeta) bundle.getItemMeta();
        meta.addItem(permissionItem(PERMISSION));
        bundle.setItemMeta(meta);
        player.getInventory().setItem(0, bundle);
        sync.sync(player);

        player.getInventory().setItem(0, null);
        sync.sync(player);

        assertTrue(permissions.current().isEmpty());
    }

    @Test
    void ignoresTheEnderChest() {
        player.getEnderChest().addItem(permissionItem(PERMISSION));

        sync.sync(player);

        assertTrue(permissions.current().isEmpty());
    }

    @Test
    void neverRevokesAPermissionItDidNotGrant() {
        sync.sync(player);

        assertTrue(permissions.revokes().isEmpty());
    }

    @Test
    void revokeAllTakesEveryGrantedPermissionAway() {
        player.getInventory().addItem(permissionItem("skysongitems.first"));
        player.getInventory().addItem(permissionItem("skysongitems.second"));
        sync.sync(player);

        sync.revokeAll(player);

        assertTrue(permissions.current().isEmpty());
        assertTrue(sync.grantedTo(player).isEmpty());
    }

    @Test
    void syncAfterRevokeAllGrantsAgain() {
        player.getInventory().addItem(permissionItem(PERMISSION));
        sync.sync(player);
        sync.revokeAll(player);

        sync.sync(player);

        assertEquals(Set.of(PERMISSION), permissions.current());
    }

    @Test
    void syncAllSyncsEveryPlayer() {
        PlayerMock other = server.addPlayer();
        player.getInventory().addItem(permissionItem("skysongitems.first"));
        other.getInventory().addItem(permissionItem("skysongitems.second"));

        sync.syncAll(List.of(player, other));

        assertEquals(Set.of("skysongitems.first"), sync.grantedTo(player));
        assertEquals(Set.of("skysongitems.second"), sync.grantedTo(other));
    }
}
