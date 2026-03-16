package org.skysongdev.skysongitems.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConsumptionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemConsumption(PlayerItemConsumeEvent event){
        if(event.getItem().getType() == Material.AIR){
            return;
        }
        PersistentDataContainer pdc = event.getItem().getItemMeta().getPersistentDataContainer();
        if(pdc.has(Constants.onConsumptionKey)){
            ArrayList<ItemStack> remainingItems = Utils.consumeItem(event.getItem());
            if(!remainingItems.isEmpty()){
                event.setReplacement(remainingItems.getFirst());
                if(remainingItems.size() > 1) {
                    for (int i = 1; i < remainingItems.size(); i++) {
                        HashMap<Integer, ItemStack> unableToAdd = event.getPlayer().getInventory().addItem(remainingItems.get(i));
                        for (Map.Entry<Integer, ItemStack> entry : unableToAdd.entrySet()) {
                            event.getPlayer().getWorld().dropItemNaturally(event.getPlayer().getLocation(), entry.getValue());
                        }
                    }
                }
            }
        }

    }
}
