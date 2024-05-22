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

import java.sql.SQLException;

public class BindBook implements CommandExecutor {
    SkysongSpecialItems plugin;
    public static NamespacedKey boundKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "bound_book");

    public BindBook(SkysongSpecialItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int id;
        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if(strings.length < 1){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Too little arguments!");
            return true;
        }
        try{
            id = Integer.parseInt(strings[0]);
        } catch (NumberFormatException e){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] ID must be numerical!");
            return true;
        }
        try{
            item.getItemMeta().getLore();
        }catch(Exception e){
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] Please hold the item you wish to assign a book to!");
            return true;
        }

        try {
            if(plugin.getDatabase().findBookByID(id) == null){
                commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Couldn't find a book with this id!");
                return true;
            }
        }catch (SQLException e){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] There's an SQL error! (Check Console)");
            e.printStackTrace();
            return true;
        }
        ItemMeta buffer = item.getItemMeta();
        PersistentDataContainer itemDataContainer = buffer.getPersistentDataContainer();
        itemDataContainer.set(boundKey, PersistentDataType.INTEGER, id);
        item.setItemMeta(buffer);
        commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Item has been bound to book id " + ChatColor.of("GOLD") + id);

        return true;
    }
}
