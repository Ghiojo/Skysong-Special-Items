package org.skysongdev.skysongitems.timer;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Decides when items' timers are checked, and nothing else: only when players deal with the
 * items, never on a clock.
 * <ul>
 *     <li>opening an inventory checks its items and the player's own;</li>
 *     <li>clicking in an inventory checks the same on the next tick, at most once per player
 *     per tick. This covers the player's own inventory screen, which the client opens
 *     without telling the server;</li>
 *     <li>using an item, or switching the held slot to it, checks that item;</li>
 *     <li>{@link #checkHeldItem} lets commands check the held item first.</li>
 * </ul>
 * An item without timers costs one lookup on its read-only data.
 */
public final class ItemTimerTriggers implements Listener {
    private final Plugin plugin;
    private final ItemTimerRunner runner;
    private final Consumer<Player> permissionItemSync;
    private final Set<UUID> pendingChecks = new HashSet<>();

    /**
     * @param plugin             schedules the next-tick checks
     * @param runner             applies the due timers
     * @param permissionItemSync grants and revokes the player's permission-item permissions
     *                           after a timer changed one of their items
     */
    public ItemTimerTriggers(Plugin plugin, ItemTimerRunner runner, Consumer<Player> permissionItemSync) {
        this.plugin = plugin;
        this.runner = runner;
        this.permissionItemSync = permissionItemSync;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player) {
            checkInventories(player, event.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClick(InventoryClickEvent event) {
        checkNextTick(event.getWhoClicked());
    }

    /** Runs first, so the other listeners see the item as its timers left it. */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != null && event.getItem() != null) {
            checkSlot(event.getPlayer(), event.getHand());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHeldSlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        runner.run(player.getInventory().getItem(event.getNewSlot()), player).ifPresent(after -> {
            player.getInventory().setItem(event.getNewSlot(), after);
            permissionItemSync.accept(player);
        });
    }

    /** Applies the due timers of the item in the player's main hand, before a command reads it. */
    public void checkHeldItem(Player player) {
        checkSlot(player, EquipmentSlot.HAND);
    }

    private void checkSlot(Player player, EquipmentSlot slot) {
        runner.run(player.getInventory().getItem(slot), player).ifPresent(after -> {
            player.getInventory().setItem(slot, after);
            permissionItemSync.accept(player);
        });
    }

    private void checkNextTick(HumanEntity entity) {
        if (entity instanceof Player player && pendingChecks.add(player.getUniqueId())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                pendingChecks.remove(player.getUniqueId());
                if (player.isOnline()) {
                    checkInventories(player, player.getOpenInventory().getTopInventory());
                }
            });
        }
    }

    /**
     * Checks the player's own items and those of the inventory they have open.
     *
     * @param open the top inventory of what the player has open; for the player's own
     *             inventory screen, that is their crafting grid, which is left alone
     */
    private void checkInventories(Player player, Inventory open) {
        if (runner.runAll(player.getInventory(), player) > 0) {
            permissionItemSync.accept(player);
        }
        if (!InventoryType.CRAFTING.equals(open.getType())) {
            runner.runAll(open, null);
        }
    }
}
