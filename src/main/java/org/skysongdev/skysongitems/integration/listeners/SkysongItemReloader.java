package org.skysongdev.skysongitems.integration.listeners;

import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronInsertEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SkysongItemReloader implements Listener {

    @EventHandler
    public void onCauldronInsertion(CauldronInsertEvent event){
        if(Utils.isSkysongItem(event.getItemSource().itemStack())){
            ItemMeta meta = event.getItemSource().itemStack().getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            String itemId = pdc.get(Constants.materialIdKey, PersistentDataType.STRING);
            String permission = Constants.materialPermissions.get(itemId);
            if(itemId != null && permission != null){
                if(!event.getPlayer().hasPermission(permission) && event.getPlayer().hasPermission("skysongspecialitems.cit")){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>You do not have the experience required to use this item!"));
                }
            }
        }
    }

    @EventHandler
    public void onCauldronExtract(CauldronExtractEvent event){
        ItemStack item = event.getItemResult().get();
        if(Utils.isSkysongItem(item)){
            item = Utils.updateItemLore(item);
        }

        event.setItemResult(item);
    }

    @EventHandler
    public void onDistilleryExtract(DistilleryExtractEvent event){
        ItemStack item = event.getTransactionSession().getResult().itemStack();
        if(Utils.isSkysongItem(item)){
            item = Utils.updateItemLore(item);
        }
        event.getTransactionSession().setResult(new ItemSource.ItemBasedSource(item));
    }

    @EventHandler
    public void onBarrelExtract(BarrelExtractEvent event){
        ItemStack item = event.getTransactionSession().getResult().itemStack();
        if(Utils.isSkysongItem(item)){
            item = Utils.updateItemLore(item);
        }
        event.getTransactionSession().setResult(new ItemSource.ItemBasedSource(item));
    }
}
