package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.skysongdev.skysongitems.utils.ItemKeys;

public class UnbindBook implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();


        try{
            item.getItemMeta().getLore();
        }catch(Exception e){
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] Please hold the item you wish to assign a book to!");
            return true;
        }
        ItemMeta buffer = item.getItemMeta();
        PersistentDataContainer itemDataContainer = buffer.getPersistentDataContainer();
        if(!itemDataContainer.has(ItemKeys.BOUND_BOOK)){
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] This item does not have a bound book!");
            return true;
        }
        itemDataContainer.remove(ItemKeys.BOUND_BOOK);
        if(itemDataContainer.has(ItemKeys.RIGHT_CLICKABLE))
            itemDataContainer.remove(ItemKeys.RIGHT_CLICKABLE);
        if(itemDataContainer.has(ItemKeys.DM_ONLY))
            itemDataContainer.remove(ItemKeys.DM_ONLY);
        item.setItemMeta(buffer);
        commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Bound books cleared from this item");
        return true;
    }
}
