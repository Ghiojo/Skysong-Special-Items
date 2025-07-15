package org.skysongdev.skysongitems.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
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
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class UpdateItemListener implements Listener {
    private final SkysongItems plugin;

    public UpdateItemListener(SkysongItems plugin) {
        this.plugin = plugin;
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemUpdate(ItemEditEvent event){
        plugin.getUpdater().UpdateItem(event.getPlayer());
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        List<Component> lore;


        lore = TextParser.TranslateToLore(Arrays.stream(container.get(Utils.loreKey, PersistentDataType.STRING).split(ChatColor.COLOR_CHAR + "")).toList(), container.get(Utils.deletedKey, PersistentDataType.STRING));
        item = event.getPlayer().getInventory().getItemInMainHand();
        item.setItemMeta(meta);

        if(container.has(Utils.propertiesKey)){
            lore.add(getPlugin().getMiniMessage().deserialize(""));

            //Update Printable Properties

            String[] currentProperties = container.get(Utils.propertiesKey, PersistentDataType.STRING).split(";");
            StringBuilder returnMessage = new StringBuilder();
            ArrayList<PrintableProperty> organizedProperties = new ArrayList<PrintableProperty>();
            for(String property : currentProperties){
                Property bufferProp = Utils.getProperty(property);
                if(!(bufferProp instanceof PrintableProperty prop)){
                    continue;
                }
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
                lore.add(getPlugin().getMiniMessage().deserialize(returnMessage.toString()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
                returnMessage.replace(0, returnMessage.length(), "");
            }

            //Update Rename Properties
            for(String property : currentProperties){
                Property bufferProp = Utils.getProperty(property);
                if(!(bufferProp instanceof RenameProperty)){
                    continue;
                }
                RenameProperty prop = (RenameProperty) bufferProp;

                if(item.getItemMeta().displayName() != null) {
                    String newName = meta.getPersistentDataContainer().get(Utils.nameKey, PersistentDataType.STRING);
                    newName = TextParser.translateHexColorCodes("&#", "", newName);
                    newName = TextParser.translateColorCodes(newName);
                    meta.displayName(getPlugin().getMiniMessage().deserialize(prop.getTag() + " " + newName).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
                }
            }

            meta.lore(lore);
        }
        item.setItemMeta(meta);
        event.getPlayer().updateInventory();

        //if(meta.getPersistentDataContainer().has(Utils.maxchargesKey)){


            /*String chargesText = "<#9C7D2C>[<#E1BD32>Charges: ";
            if(meta.getPersistentDataContainer().get(Utils.chargesKey, PersistentDataType.INTEGER) == 0){
                chargesText = chargesText + "<#B54032>0";
            } else{
                chargesText = chargesText + "<#E1BD32>" + meta.getPersistentDataContainer().get(Utils.chargesKey, PersistentDataType.INTEGER).toString();
            }
            chargesText = chargesText + "<#E1BD32>/" + meta.getPersistentDataContainer().get(Utils.maxchargesKey, PersistentDataType.INTEGER) + "<#9C7D2C>]";
            lore.add(chargesText);*/


        //}
    }

}
