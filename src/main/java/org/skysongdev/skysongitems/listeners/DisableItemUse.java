package org.skysongdev.skysongitems.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.ItemKeys;

public class DisableItemUse implements Listener {

    @EventHandler
    public void onPlayerItemUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        if (item.getItemMeta().getPersistentDataContainer().has(ItemKeys.DISABLED)
                && item.getItemMeta().getPersistentDataContainer().get(ItemKeys.DISABLED, PersistentDataType.BOOLEAN)) {
            event.setCancelled(true);
        }
    }
}
