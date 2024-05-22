package org.skysongdev.skysongspecialitems.events;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongspecialitems.SkysongSpecialItems;

public class DisableItemUse implements Listener {
    public static NamespacedKey disabledKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "is_disabled");

    @EventHandler
    public void onPlayerItemUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        if (item.getItemMeta().getPersistentDataContainer().has(disabledKey)) {
            if (item.getItemMeta().getPersistentDataContainer().get(disabledKey, PersistentDataType.BOOLEAN))
                event.setCancelled(true);
        }
    }
}
