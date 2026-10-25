package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.database.BookDescriptions;

import java.sql.SQLException;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetBookContent implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int id;
        Player player = (Player) commandSender;

        ItemStack item = player.getInventory().getItemInMainHand();
        BookDescriptions bookDescriptions;

        try{
            id = Integer.parseInt(strings[0]);
        } catch (NumberFormatException e){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] ID must be numerical!");
            return true;
        }

        if(item.getType() == Material.WRITTEN_BOOK){
            bookDescriptions = new BookDescriptions(item);
        } else{
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Please hold a written book to save!");
            return true;
        }
        bookDescriptions.setId(id);
        try {
            getPlugin().getBookRepository().update(bookDescriptions);
        }catch(SQLException e){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Something went wrong when saving this book! (Check Database)");
            return true;
        }

        commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" + ChatColor.of("GRAY") + "Book has been edited!");
        return true;
    }
}
