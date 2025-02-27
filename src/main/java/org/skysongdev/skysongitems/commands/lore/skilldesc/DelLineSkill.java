package org.skysongdev.skysongitems.commands.lore.skilldesc;

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

import static java.lang.Integer.parseInt;
import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class DelLineSkill implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        List<String> newItemLore = new ArrayList<String>();
        String bufferString = "";
        try{
            parseInt(strings[0]);
        } catch(NumberFormatException e){
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] Please type an actual number of line!");
            return false;
        }

        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("<red>Please hold the item you wish to lore!"));
            return true;
        }

        buffer = item.getItemMeta();
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();

        newItemLore = new ArrayList<String>(Arrays.stream(bufferDataContainer.get(Utils.loreKey, PersistentDataType.STRING).split(ChatColor.COLOR_CHAR + "")).toList());
        newItemLore = TextParser.deleteLine(1, parseInt(strings[0]), newItemLore);
        for(String string : newItemLore){
            bufferString += ChatColor.COLOR_CHAR + string;
        }
        bufferDataContainer.set(Utils.loreKey, PersistentDataType.STRING, bufferString.substring(1));
        buffer.lore(TextParser.TranslateToLore(newItemLore, bufferDataContainer.get(TextParser.deletedKey, PersistentDataType.STRING)));

        item.setItemMeta(buffer);
        return true;
    }
}
