package org.skysongdev.skysongitems.commands.lore.physicaldesc;

import org.bukkit.Material;
import org.skysongdev.skysongitems.utils.TextParser;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetPhysical implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        List<String> newItemLore = new ArrayList<String>();
        String bufferString = "";
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        if(strings.length < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Please type in lore for the item!"));
            return true;
        }
        getPlugin().getUpdater().UpdateLore(player);

        buffer = item.getItemMeta();
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();

        if(bufferDataContainer.isEmpty() || bufferDataContainer.get(TextParser.deletedKey, PersistentDataType.STRING) == null){
            newItemLore = new ArrayList<String>(Arrays.stream(bufferDataContainer.get(Utils.loreKey, PersistentDataType.STRING).split(ChatColor.COLOR_CHAR + "")).toList());
            newItemLore = TextParser.manipulateSection(newItemLore, strings, 0, true);
            for(String string : newItemLore){
                bufferString += ChatColor.COLOR_CHAR + string;
            }
            bufferDataContainer.set(Utils.loreKey, PersistentDataType.STRING, bufferString.substring(1));
            buffer.lore(TextParser.TranslateToLore(newItemLore));
        } else{
            String deletedString = bufferDataContainer.get(TextParser.deletedKey, PersistentDataType.STRING);
            newItemLore = new ArrayList<String>(Arrays.stream(bufferDataContainer.get(Utils.loreKey, PersistentDataType.STRING).split(ChatColor.COLOR_CHAR + "")).toList());
            newItemLore = TextParser.manipulateSection(newItemLore, strings, 0, true);
            for(String string : newItemLore){
                bufferString += ChatColor.COLOR_CHAR + string;
            }
            bufferDataContainer.set(Utils.loreKey, PersistentDataType.STRING, bufferString.substring(1));
            buffer.lore(TextParser.TranslateToLore(newItemLore, deletedString));
        }
        item.setItemMeta(buffer);
        player.updateInventory();

        return true;
    }
}
