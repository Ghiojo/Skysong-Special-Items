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

public class UnbindBook implements CommandExecutor {
    public static NamespacedKey boundKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "bound_book");
    public static NamespacedKey dMOnlyKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "dm_only");
    public static NamespacedKey rightClickKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "is_right_clickable");
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
        if(!itemDataContainer.has(boundKey)){
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] This item does not have a bound book!");
            return true;
        }
        itemDataContainer.remove(boundKey);
        if(itemDataContainer.has(rightClickKey))
            itemDataContainer.remove(rightClickKey);
        if(itemDataContainer.has(dMOnlyKey))
            itemDataContainer.remove(dMOnlyKey);
        item.setItemMeta(buffer);
        commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Bound books cleared from this item");
        return true;
    }
}
