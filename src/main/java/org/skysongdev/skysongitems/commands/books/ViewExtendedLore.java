package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.ItemKeys;

public class ViewExtendedLore implements CommandExecutor {
    private final OpenBook openBook;

    public ViewExtendedLore(OpenBook openBook) {
        this.openBook = openBook;
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
        if(!item.getItemMeta().getPersistentDataContainer().has(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER)){
            commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY").toString() + "This item doesn't have an extended description");
            return true;
        }
        if(item.getItemMeta().getPersistentDataContainer().has(ItemKeys.DM_ONLY, PersistentDataType.BOOLEAN) && item.getItemMeta().getPersistentDataContainer().get(ItemKeys.DM_ONLY, PersistentDataType.BOOLEAN)){
            if(!commandSender.hasPermission("skysongspecialitems.dm")){
                commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("RED").toString() + "You do not have permission to view this lore");
                return true;
            }
        }
        int idToOpen = item.getItemMeta().getPersistentDataContainer().get(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER);
        String[] args = {String.valueOf(idToOpen)};
        openBook.onCommand(commandSender, command, s, args);

        return true;
    }
}
