package org.skysongdev.skysongitems.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.plugin.Plugin;
import org.skysongdev.skysongitems.utils.PermissionItemSync;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Keeps a player's item permissions in step with the permission items in their
 * inventory: they have an item's permission while the item is in their
 * inventory, and only then.
 *
 * <p>The events below are the usual ways an inventory changes. Most of them fire
 * before the change happens, and may still be cancelled by another plugin, so
 * each one only asks for a {@link PermissionItemSync#sync} on the next tick, which
 * then reads what the inventory really holds. A check of every online player
 * every {@link #CHECK_INTERVAL_TICKS} ticks catches the changes no event reports,
 * such as /clear or another plugin taking the item.
 */
public class PermissionItemListener implements Listener {
    /** One second. */
    static final long CHECK_INTERVAL_TICKS = 20L;

    private final Plugin plugin;
    private final PermissionItemSync sync;
    private final Set<UUID> pendingSyncs = new HashSet<>();

    public PermissionItemListener(Plugin plugin, PermissionItemSync sync) {
        this.plugin = plugin;
        this.sync = sync;
    }

    /**
     * Registers this listener, starts the periodic check, and syncs the players
     * already online, who are there when the plugin is reloaded.
     */
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::syncOnlinePlayers,
                CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
        syncOnlinePlayers();
    }

    /** Revokes every item permission of the online players. Call this when the plugin is disabled. */
    public void revokeOnlinePlayers() {
        plugin.getServer().getOnlinePlayers().forEach(sync::revokeAll);
    }

    /** Syncs the player now, for changes the plugin makes itself, such as setting an item's permission tag. */
    public void syncNow(Player player) {
        sync.sync(player);
    }

    private void syncOnlinePlayers() {
        sync.syncAll(plugin.getServer().getOnlinePlayers());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        sync.sync(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        pendingSyncs.remove(event.getPlayer().getUniqueId());
        sync.revokeAll(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        syncNextTick(event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryDrag(InventoryDragEvent event) {
        syncNextTick(event.getWhoClicked());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemDrop(PlayerDropItemEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemPickup(EntityPickupItemEvent event) {
        syncNextTick(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemBreak(PlayerItemBreakEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        syncNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Entity shooter) {
            syncNextTick(shooter);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        syncNextTick(event.getEntity());
    }

    /** Syncs the player on the next tick, once, however many events ask for it in this tick. */
    private void syncNextTick(Entity entity) {
        if (entity instanceof Player player && pendingSyncs.add(player.getUniqueId())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                pendingSyncs.remove(player.getUniqueId());
                if (player.isOnline()) {
                    sync.sync(player);
                }
            });
        }
    }
}
