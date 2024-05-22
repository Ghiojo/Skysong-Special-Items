package org.skysongdev.skysongspecialitems.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.skysongdev.skysongspecialitems.SkysongSpecialItems;
import org.skysongdev.skysongspecialitems.database.BookDescriptions;
import org.skysongdev.skysongspecialitems.database.Database;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class SaveBook implements CommandExecutor {
    private final SkysongSpecialItems plugin;
    public SaveBook(SkysongSpecialItems pluginin) {
        this.plugin = pluginin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;

        ItemStack item = player.getInventory().getItemInMainHand();
        BookDescriptions bookDescriptions;

        if(item.getType() == Material.WRITTEN_BOOK){
            bookDescriptions = new BookDescriptions(item);
        } else{
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Please hold a written book to save!");
            return true;
        }
        int bookSaveID;
        try {
            plugin.getDatabase().createBookDescription(bookDescriptions);
            bookSaveID = plugin.getDatabase().findLatestId();
        }catch(SQLException e){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Something went wrong when saving this book! (Check Database)");
            e.printStackTrace();
            return true;
        }


        commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" + ChatColor.of("GRAY") + "Book has been saved to the database with the ID (" + bookSaveID + ")");
        return true;
    }
}
