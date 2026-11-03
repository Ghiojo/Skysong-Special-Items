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

import java.util.List;
import java.util.Objects;
import org.skysongdev.skysongitems.utils.ItemKeys;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class Updater {
    //TODO: Make this Updater be able to turn items without their nbt lore buffer into items with their nbt lore buffer
    public void UpdateItem(Player player){
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold an item!"));
            return;
        }
        if(isCurrent(item))
            return;
        player.getInventory().setItemInMainHand(upgrade(item));
    }

    /**
     * Brings the item up to the current item version, running every update step
     * from the version it has. An item without a version is treated as version 0,
     * so a plain item becomes a SkysongItems item. A current item is returned as it is.
     *
     * @return the upgraded item, which is the given item changed in place
     */
    public ItemStack upgrade(ItemStack item){
        if(isCurrent(item))
            return item;

        // Add a branch for each item version when a new version for items is added.
        String version = item.getPersistentDataContainer().get(ItemKeys.VERSION, PersistentDataType.STRING);
        if("1.0".equals(version)){
            return UpdateOnePointOne(item);
        }
        return UpdateAll(item);
    }

    private static boolean isCurrent(ItemStack item){
        return Objects.equals(item.getPersistentDataContainer().get(ItemKeys.VERSION, PersistentDataType.STRING), Constants.ITEM_VERSION);
    }

    public ItemStack UpdateZero(ItemStack item){
        ItemStack newItem = item;
        ItemMeta buffer = newItem.getItemMeta();
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();
        ItemData data = new ItemData(bufferDataContainer);
        List<String> parsedLore;
        parsedLore = buffer.getLore();

        parsedLore = TextParser.TranslateFromLore(parsedLore, data.getDeletedLines());

        // Store every section in its slot, even when two sections have the same text.
        data.setLoreSections(parsedLore);
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
