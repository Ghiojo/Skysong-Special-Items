package org.skysongdev.skysongitems.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.skysongdev.skysongitems.testsupport.MockBukkitTest;
import org.skysongdev.skysongitems.testsupport.RecordedPermissions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.skysongdev.skysongitems.testsupport.RecordedPermissions.permissionItem;

/**
 * Covers when the listener checks a player's permission items: on join, on the
 * tick after an inventory event, every second (spread over the ticks) for changes no event reports,
 * and on quit and plugin disable, when everything is revoked.
 */
class PermissionItemListenerTest extends MockBukkitTest {
    private static final String PERMISSION = "skysongitems.test";

    private RecordedPermissions permissions;
    private PermissionItemListener listener;
    private PlayerMock player;

    @BeforeEach
    void createListener() {
        permissions = new RecordedPermissions();
        listener = new PermissionItemListener(MockBukkit.createMockPlugin(), permissions.newSync());
        player = server.addPlayer();
    }

    @Test
    void registeringSyncsThePlayersAlreadyOnline() {
        player.getInventory().addItem(permissionItem(PERMISSION));

        listener.register();

        assertEquals(Set.of(PERMISSION), permissions.current());
    }

    @Test
    void joiningSyncsRightAway() {
        listener.register();
        PlayerMock joining = new PlayerMock(server, "Holder");
        joining.getInventory().addItem(permissionItem(PERMISSION));

        server.addPlayer(joining);

        assertEquals(Set.of(PERMISSION), permissions.current());
    }

    @Test
    void anInventoryEventSyncsOnTheNextTick() {
        listener.register();
        player.getInventory().addItem(permissionItem(PERMISSION));

        listener.onInventoryClose(new InventoryCloseEvent(player.getOpenInventory()));
        assertTrue(permissions.current().isEmpty());

        server.getScheduler().performOneTick();
        assertEquals(Set.of(PERMISSION), permissions.current());
    }

    @Test
    void severalEventsInOneTickSyncOnce() {
        listener.register();
        player.getInventory().addItem(permissionItem(PERMISSION));

        listener.onInventoryClose(new InventoryCloseEvent(player.getOpenInventory()));
        listener.onInventoryClose(new InventoryCloseEvent(player.getOpenInventory()));
        server.getScheduler().performOneTick();
        player.getInventory().clear();
        server.getScheduler().performOneTick();

        // A second queued sync would have run on the second tick and revoked.
        assertEquals(Set.of(PERMISSION), permissions.current());
    }

    @Test
    void aCancelledPickupGrantsNothing() {
        listener.register();
        Item dropped = player.getWorld().dropItem(player.getLocation(), permissionItem(PERMISSION));
        EntityPickupItemEvent pickup = new EntityPickupItemEvent(player, dropped, 0);
        pickup.setCancelled(true);

        listener.onItemPickup(pickup);
        server.getScheduler().performOneTick();

        assertTrue(permissions.current().isEmpty());
    }

    @Test
    void thePeriodicCheckRevokesAnItemRemovedWithoutAnyEvent() {
        player.getInventory().addItem(permissionItem(PERMISSION));
        listener.register();

        player.getInventory().clear();
        server.getScheduler().performTicks(PermissionItemListener.CHECK_INTERVAL_TICKS);

        assertTrue(permissions.current().isEmpty());
    }

    @Test
    void thePeriodicCheckSpreadsThePlayersOverTheInterval() {
        List<PlayerMock> players = new ArrayList<>(List.of(player));
        for (int added = 1; added < 40; added++) {
            players.add(server.addPlayer());
        }
        for (int index = 0; index < players.size(); index++) {
            players.get(index).getInventory().addItem(permissionItem("skysongitems.player" + index));
        }
        listener.register();
        players.forEach(online -> online.getInventory().clear());

        server.getScheduler().performOneTick();
        // 40 players over 20 ticks: two each tick.
        assertEquals(2, permissions.revokes().size());

        server.getScheduler().performTicks(PermissionItemListener.CHECK_INTERVAL_TICKS - 1);
        assertTrue(permissions.current().isEmpty());
    }

    @Test
    void quittingRevokesEverything() {
        player.getInventory().addItem(permissionItem(PERMISSION));
        listener.register();

        listener.onPlayerQuit(new PlayerQuitEvent(player, Component.empty(), PlayerQuitEvent.QuitReason.DISCONNECTED));

        assertTrue(permissions.current().isEmpty());
    }

    @Test
    void disablingRevokesEveryOnlinePlayer() {
        PlayerMock other = server.addPlayer();
        player.getInventory().addItem(permissionItem("skysongitems.first"));
        other.getInventory().addItem(permissionItem("skysongitems.second"));
        listener.register();

        listener.revokeOnlinePlayers();

        assertTrue(permissions.current().isEmpty());
        assertEquals(List.of("skysongitems.first", "skysongitems.second"),
                permissions.revokes().stream().sorted().toList());
    }

    @Test
    void syncNowAppliesATagChangeWithoutWaiting() {
        listener.register();
        player.getInventory().addItem(permissionItem(PERMISSION));

        listener.syncNow(player);

        assertEquals(Set.of(PERMISSION), permissions.current());
    }
}
