package org.skysongdev.skysongitems.listeners;

import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.*;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class UpdateItemListener implements Listener {
    private final SkysongItems plugin;

    //TODO: Why the fuck are we doing the same thing twice? Remove that Item Lore Manipulation, and update this method to work with Tags
    public UpdateItemListener(SkysongItems plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemUpdate(ItemEditEvent event){
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        List<Component> lore;
        plugin.getUpdater().UpdateLore(event.getPlayer());

        if(meta.getPersistentDataContainer().has(Utils.maxchargesKey)){
            item = event.getPlayer().getInventory().getItemInMainHand();
            item.setItemMeta(meta);
            lore = meta.lore();
            lore.add(getPlugin().getMiniMessage().deserialize(""));

            /*String chargesText = "<#9C7D2C>[<#E1BD32>Charges: ";
            if(meta.getPersistentDataContainer().get(Utils.chargesKey, PersistentDataType.INTEGER) == 0){
                chargesText = chargesText + "<#B54032>0";
            } else{
                chargesText = chargesText + "<#E1BD32>" + meta.getPersistentDataContainer().get(Utils.chargesKey, PersistentDataType.INTEGER).toString();
            }
            chargesText = chargesText + "<#E1BD32>/" + meta.getPersistentDataContainer().get(Utils.maxchargesKey, PersistentDataType.INTEGER) + "<#9C7D2C>]";
            lore.add(chargesText);*/

            if(meta.getPersistentDataContainer().has(Utils.propertiesKey)){
                //Update Printable Properties
                String[] currentProperties = meta.getPersistentDataContainer().get(Utils.propertiesKey, PersistentDataType.STRING).split(";");
                StringBuilder returnMessage = new StringBuilder();
                ArrayList<PrintableProperty> organizedProperties = new ArrayList<PrintableProperty>();
                for(String property : currentProperties){
                    Property bufferProp = Utils.getProperty(property);
                    if(!(bufferProp instanceof PrintableProperty)){
                        continue;
                    }
                    PrintableProperty prop = (PrintableProperty) bufferProp;
                    if(organizedProperties.isEmpty())
                        organizedProperties.add(prop);
                    else{
                        boolean added = false;
                        for(int i = 0; i < organizedProperties.size(); i++){
                            if(organizedProperties.get(i).getWeight() < prop.getWeight()){
                                organizedProperties.add(i, prop);
                                added = true;
                                break;
                            }
                        }
                        if(!added){
                            organizedProperties.add(prop);
                        }
                    }
                }
                for (PrintableProperty property : organizedProperties) {
                    returnMessage.append(property.getParsedPrinting(item));
                    lore.add(getPlugin().getMiniMessage().deserialize(returnMessage.toString()));
                    returnMessage.replace(0, returnMessage.length(), "");
                }

                //Update Rename Properties
                for(String property : currentProperties){
                    Property bufferProp = Utils.getProperty(property);
                    if(!(bufferProp instanceof RenameProperty)){
                        continue;
                    }
                    RenameProperty prop = (RenameProperty) bufferProp;

                    if(item.getItemMeta().displayName() != null)
                        meta.itemName(getPlugin().getMiniMessage().deserialize(prop.getTag()).append(meta.displayName()));
                }
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
    }

}
