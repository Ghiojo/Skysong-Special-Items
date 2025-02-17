package org.skysongdev.skysongitems.commands.lore;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.TextParser;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class Updater {
    //TODO: Make this Updater be able to turn items without their nbt lore buffer into items with their nbt lore buffer
    public void UpdateLore(Player player){
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        PersistentDataContainer bufferDataContainer;
        if(item == null || item.getType() == Material.AIR){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Please hold an item!"));
            return;
        }
        buffer = item.getItemMeta();
        bufferDataContainer = buffer.getPersistentDataContainer();

        if(Objects.equals(bufferDataContainer.get(Utils.versionKey, PersistentDataType.STRING), Utils.ITEM_VERSION))
            return;

        if(!(bufferDataContainer.has(Utils.versionKey))){
            item = UpdateAll(item);
        }

        //Uncomment and add more cases when a new version for items is added
        /*switch(bufferDataContainer.get(Utils.versionKey, PersistentDataType.STRING)){

        }*/
        player.getInventory().setItemInMainHand(item);
    }

    public ItemStack UpdateZero(ItemStack item){
        ItemStack newItem = item;
        ItemMeta buffer = newItem.getItemMeta();
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();
        List<String> parsedLore;
        StringBuilder newProperty = new StringBuilder();
        parsedLore = buffer.getLore();

        if(bufferDataContainer.has(TextParser.deletedKey)){
            parsedLore = TextParser.TranslateFromLore(parsedLore, bufferDataContainer.get(TextParser.deletedKey, PersistentDataType.STRING));
        } else {
            parsedLore = TextParser.TranslateFromLore(parsedLore);
        }
        newProperty.append(parsedLore.get(0));
        for(String section : parsedLore){
            if(section.equals(parsedLore.get(0))){
                continue;
            }
            newProperty.append(ChatColor.COLOR_CHAR + section);
        }
        bufferDataContainer.set(Utils.loreKey, PersistentDataType.STRING, newProperty.toString());
        bufferDataContainer.set(Utils.versionKey, PersistentDataType.STRING, Utils.ITEM_VERSION);

        if(buffer.displayName() != null){
            String name = buffer.getDisplayName();
            name = TextParser.revertColor(name);
            bufferDataContainer.set(Utils.nameKey, PersistentDataType.STRING, name);
        }

        if(bufferDataContainer.has(TextParser.deletedKey)){
            buffer.lore(TextParser.TranslateToLore(parsedLore, bufferDataContainer.get(TextParser.deletedKey, PersistentDataType.STRING)));
        }
        else {
            buffer.lore(TextParser.TranslateToLore(parsedLore));
        }

        if(bufferDataContainer.has(Utils.chargesKey)){
            bufferDataContainer.set(Utils.propertiesKey, PersistentDataType.STRING, "charges");
        }
        newItem.setItemMeta(buffer);

        return newItem;
    }

    //Add every update method here when a new update to items occurs.
    public ItemStack UpdateAll(ItemStack item){
        ItemStack finishedItem = item;

        finishedItem = UpdateZero(finishedItem);

        return finishedItem;
    }
}
