package org.skysongdev.skysongspecialitems.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongspecialitems.SkysongSpecialItems;

public class ViewExtendedLore implements CommandExecutor {
    SkysongItems parent;
    public static NamespacedKey boundKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "bound_book");
    public static NamespacedKey dMOnlyKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "dm_only");

    public ViewExtendedLore(SkysongItems parent) {
        this.parent = parent;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        try{
            item.getItemMeta().getLore();
        }catch(Exception e){
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] Please hold an item in your main hand!");
            return true;
        }
        if(!item.getItemMeta().getPersistentDataContainer().has(boundKey, PersistentDataType.INTEGER)){
            commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY").toString() + "This item doesn't have an extended description");
            return true;
        }
        if(item.getItemMeta().getPersistentDataContainer().has(dMOnlyKey, PersistentDataType.BOOLEAN) && item.getItemMeta().getPersistentDataContainer().get(dMOnlyKey, PersistentDataType.BOOLEAN)){
            if(!commandSender.hasPermission("skysongspecialitems.dm")){
                commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("RED").toString() + "You do not have permission to view this lore");
                return true;
            }
        }
        int idToOpen = item.getItemMeta().getPersistentDataContainer().get(boundKey, PersistentDataType.INTEGER);
        String[] args = {String.valueOf(idToOpen)};
        parent.openBook.onCommand(commandSender, command, s, args);

        return true;
    }
}
