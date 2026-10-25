package org.skysongdev.skysongitems.integration.listeners;

import dev.jsinco.brewery.bukkit.api.event.transaction.BarrelExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronExtractEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.CauldronInsertEvent;
import dev.jsinco.brewery.bukkit.api.event.transaction.DistilleryExtractEvent;
import dev.jsinco.brewery.bukkit.api.transaction.ItemSource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemLoreRenderer;
import org.skysongdev.skysongitems.utils.ItemFactory;
import org.skysongdev.skysongitems.utils.ItemKeys;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SkysongItemReloader implements Listener {

    @EventHandler
    public void onCauldronInsertion(CauldronInsertEvent event){
        ItemStack item = event.getItemSource().itemStack();
        if(!ItemFactory.isSkysongItem(item)){
            return;
        }
        String itemId = item.getItemMeta().getPersistentDataContainer().get(ItemKeys.MATERIAL_ID, PersistentDataType.STRING);
        if(!getPlugin().getMaterialPermissions().canUse(event.getPlayer(), itemId)){
            event.setCancelled(true);
            event.getPlayer().sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>You do not have the experience required to use this item!"));
        }
    }

    @EventHandler
    public void onCauldronExtract(CauldronExtractEvent event){
        ItemStack item = event.getItemResult().get();
        if(ItemFactory.isSkysongItem(item)){
            item = ItemLoreRenderer.refresh(item);
        }

        event.setItemResult(item);
    }

    @EventHandler
    public void onDistilleryExtract(DistilleryExtractEvent event){
        ItemStack item = event.getTransactionSession().getResult().itemStack();
        if(ItemFactory.isSkysongItem(item)){
            item = ItemLoreRenderer.refresh(item);
        }
        event.getTransactionSession().setResult(new ItemSource.ItemBasedSource(item));
    }

    @EventHandler
    public void onBarrelExtract(BarrelExtractEvent event){
        ItemStack item = event.getTransactionSession().getResult().itemStack();
        if(ItemFactory.isSkysongItem(item)){
            item = ItemLoreRenderer.refresh(item);
        }
        event.getTransactionSession().setResult(new ItemSource.ItemBasedSource(item));
    }
}
