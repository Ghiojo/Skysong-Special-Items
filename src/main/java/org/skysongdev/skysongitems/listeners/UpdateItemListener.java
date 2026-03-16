package org.skysongdev.skysongitems.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.*;

public class UpdateItemListener implements Listener {
    private final SkysongItems plugin;

    public UpdateItemListener(SkysongItems plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemUpdate(ItemEditEvent event){
        plugin.getUpdater().UpdateItem(event.getPlayer());
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();

        item = Utils.updateItemLore(item);
        event.getPlayer().getInventory().setItemInMainHand(item);
        event.getPlayer().updateInventory();
    }

}
