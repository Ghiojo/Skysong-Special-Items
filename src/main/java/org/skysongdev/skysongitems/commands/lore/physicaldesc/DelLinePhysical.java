package org.skysongdev.skysongitems.commands.lore.physicaldesc;

import org.bukkit.Material;
import org.skysongdev.skysongitems.utils.TextParser;
import org.skysongdev.skysongitems.utils.ItemData;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;
import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class DelLinePhysical implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        List<String> newItemLore = new ArrayList<String>();
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
        ItemData data = ItemData.from(buffer);
        newItemLore = TextParser.deleteLine(0, parseInt(strings[0]), data.getLoreSections());
        data.setLoreSections(newItemLore);
        buffer.lore(TextParser.TranslateToLore(newItemLore, data.getDeletedLines()));

        item.setItemMeta(buffer);
        return true;
    }
}
