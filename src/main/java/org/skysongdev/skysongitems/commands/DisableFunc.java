package org.skysongdev.skysongitems.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.ItemKeys;

public class DisableFunc implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        try{
            item.getItemMeta();
        }catch(Exception e){
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] Please hold an item!");
            return true;
        }
        ItemMeta buffer = item.getItemMeta();
        assert buffer != null;
        PersistentDataContainer dataContainer = buffer.getPersistentDataContainer();
        if (dataContainer.has(ItemKeys.DISABLED, PersistentDataType.BOOLEAN)) {
            if(dataContainer.get(ItemKeys.DISABLED, PersistentDataType.BOOLEAN)){
                dataContainer.set(ItemKeys.DISABLED, PersistentDataType.BOOLEAN, false);
                player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Item functions have been " + ChatColor.of("GREEN") + "enabled");
            }
            else{
                dataContainer.set(ItemKeys.DISABLED, PersistentDataType.BOOLEAN, true);
                player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Item functions have been " + ChatColor.of("RED") + "disabled");
            }

        } else {
            dataContainer.set(ItemKeys.DISABLED, PersistentDataType.BOOLEAN, true);
            player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Item functions have been " + ChatColor.of("RED") + "disabled");
        }
        item.setItemMeta(buffer);

        return true;
    }
}
