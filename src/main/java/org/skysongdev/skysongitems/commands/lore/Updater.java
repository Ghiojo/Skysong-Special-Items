package org.skysongdev.skysongitems.commands.lore;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.TextParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.skysongdev.skysongitems.utils.ItemKeys;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class Updater {
    //TODO: Make this Updater be able to turn items without their nbt lore buffer into items with their nbt lore buffer
    public void UpdateItem(Player player){
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        PersistentDataContainer bufferDataContainer;
        if(item == null || item.getType() == Material.AIR){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold an item!"));
            return;
        }
        buffer = item.getItemMeta();
        bufferDataContainer = buffer.getPersistentDataContainer();

        if(Objects.equals(bufferDataContainer.get(ItemKeys.VERSION, PersistentDataType.STRING), Constants.ITEM_VERSION))
            return;

        //Uncomment and add more cases when a new version for items is added
        String version = bufferDataContainer.get(ItemKeys.VERSION, PersistentDataType.STRING);
        if(version == null || version.isEmpty()){
            version = "0";
        }
        switch(version){
            case "1.0":
                item = UpdateOnePointOne(item);
                break;
            default:
                item = UpdateAll(item);
                break;
        }
        player.getInventory().setItemInMainHand(item);
    }

    public ItemStack UpdateZero(ItemStack item){
        ItemStack newItem = item;
        ItemMeta buffer = newItem.getItemMeta();
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();
        ItemData data = new ItemData(bufferDataContainer);
        List<String> parsedLore;
        List<String> loreSections = new ArrayList<>();
        parsedLore = buffer.getLore();

        parsedLore = TextParser.TranslateFromLore(parsedLore, data.getDeletedLines());

        loreSections.add(parsedLore.get(0));
        for(String section : parsedLore){
            if(section.equals(parsedLore.get(0))){
                continue;
            }
            loreSections.add(section);
        }
        data.setLoreSections(loreSections);
        bufferDataContainer.set(ItemKeys.VERSION, PersistentDataType.STRING, "1.0");

        if(buffer.displayName() != null){
            String name = buffer.getDisplayName();
            name = TextParser.revertColor(name);
            bufferDataContainer.set(ItemKeys.NAME, PersistentDataType.STRING, name);
        }


        buffer.lore(TextParser.TranslateToLore(parsedLore, data.getDeletedLines()));


        if(bufferDataContainer.has(ItemKeys.CHARGES)){
            data.setProperties(List.of("charges"));
        }
        newItem.setItemMeta(buffer);

        return newItem;
    }

    public ItemStack UpdateOnePointOne(ItemStack item){
        ItemStack newItem = item;
        ItemMeta buffer = newItem.getItemMeta();
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();

        if(bufferDataContainer.has(ItemKeys.NAME, PersistentDataType.STRING)) {
            String bufferString = bufferDataContainer.get(ItemKeys.NAME, PersistentDataType.STRING);

            bufferString = TextParser.translateHexColorCodes("&#", "", bufferString);
            bufferString = TextParser.translateColorCodes(bufferString);

            buffer.displayName(getPlugin().getMiniMessage().deserialize(bufferString.trim()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));
        }

        bufferDataContainer.set(ItemKeys.VERSION, PersistentDataType.STRING, Constants.ITEM_VERSION);

        newItem.setItemMeta(buffer);

        return newItem;
    }

    //Add every update method here when a new update to items occurs.
    public ItemStack UpdateAll(ItemStack item){
        ItemStack finishedItem = item;

        finishedItem = UpdateZero(finishedItem);
        finishedItem = UpdateOnePointOne(finishedItem);

        return finishedItem;
    }
}
