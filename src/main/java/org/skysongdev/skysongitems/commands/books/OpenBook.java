package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.skysongdev.skysongitems.database.BookDescriptions;
import org.skysongdev.skysongitems.database.BookQueries;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class OpenBook implements CommandExecutor {
    private final BookQueries bookQueries;

    public OpenBook(BookQueries bookQueries) {
        this.bookQueries = bookQueries;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int id;
        Player player = (Player) commandSender;

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

        bookQueries.submit(books -> books.findById(id),
                book -> openFoundBook(player, book),
                e -> reportSqlError(player, e));
        return true;
    }

    private static void openFoundBook(Player player, BookDescriptions book) {
        if(!player.isOnline()){
            return;
        }
        if(book == null){
            player.sendMessage(ChatColor.of("RED") + "[ERROR] Couldn't find a book with this id!");
            return;
        }

        ItemStack tempBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta tempMeta = (BookMeta) tempBook.getItemMeta();
        List<String> pages = Arrays.asList(book.getBookcontent().split("\\[PageEnd\\]"));
        tempMeta.setPages(pages);
        tempMeta.setTitle(book.getName());
        tempMeta.setAuthor("Skysong");
        tempBook.setItemMeta(tempMeta);

        player.openBook(tempBook);
    }

    private static void reportSqlError(CommandSender commandSender, SQLException e) {
        commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] There's an SQL error! (Check Console)");
        e.printStackTrace();
    }
}
