package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.skysongdev.skysongitems.SkysongItems;

import java.sql.SQLException;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class DeleteBook implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int id;
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

        try {
            getPlugin().getBookRepository().delete(id);
        }catch (SQLException e){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Cannot find book with this id");
            e.printStackTrace();
        }

        commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" + ChatColor.of("GRAY") + "Book with id (" + id + ") has been deleted from the database");
        return true;
    }
}
