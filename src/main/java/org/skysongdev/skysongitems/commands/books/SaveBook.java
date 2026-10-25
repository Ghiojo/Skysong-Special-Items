package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.database.BookDescriptions;
import org.skysongdev.skysongitems.database.BookQueries;

public class SaveBook implements CommandExecutor {
    private final BookQueries bookQueries;

    public SaveBook(BookQueries bookQueries) {
        this.bookQueries = bookQueries;
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
        // Both queries run in one task, so no other book can be saved between the insert and reading its id.
        bookQueries.submit(books -> {
                    books.create(bookDescriptions);
                    return books.findLatestId();
                },
                bookSaveID -> commandSender.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "]" + ChatColor.of("GRAY") + "Book has been saved to the database with the ID (" + bookSaveID + ")"),
                e -> {
                    commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] Something went wrong when saving this book! (Check Database)");
                    e.printStackTrace();
                });
        return true;
    }
}
