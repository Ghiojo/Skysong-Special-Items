package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.database.BookDescriptions;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class OpenBook implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int id;
        Player player = (Player) commandSender;
        BookDescriptions book;

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
            book = getPlugin().getBookRepository().findById(id);
            if(book == null){
                commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Couldn't find a book with this id!");
                return true;
            }
        }catch (SQLException e){
            commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] There's an SQL error! (Check Console)");
            e.printStackTrace();
            return true;
        }

        ItemStack tempBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta tempMeta = (BookMeta) tempBook.getItemMeta();
        List<String> pages = Arrays.asList(book.getBookcontent().split("\\[PageEnd\\]"));
        tempMeta.setPages(pages);
        tempMeta.setTitle(book.getName());
        tempMeta.setAuthor("Skysong");
        tempBook.setItemMeta(tempMeta);

        player.openBook(tempBook);

        return true;
    }
}
