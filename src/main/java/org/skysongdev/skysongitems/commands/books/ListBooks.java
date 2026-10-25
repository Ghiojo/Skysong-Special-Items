package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.skysongdev.skysongitems.database.BookDescriptionRepository;
import org.skysongdev.skysongitems.database.BookDescriptions;
import org.skysongdev.skysongitems.database.BookQueries;

import java.util.List;

public class ListBooks implements CommandExecutor {
    private final BookQueries bookQueries;

    public ListBooks(BookQueries bookQueries) {
        this.bookQueries = bookQueries;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        int index = 1;
        if(strings.length > 0) {
            try {
                if (strings != null)
                    index = Integer.parseInt(strings[0]);
            } catch (NumberFormatException e) {
                commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] ID must be numerical!");
                return true;
            }
        }

        int page = index;
        bookQueries.submit(BookDescriptionRepository::listAll,
                books -> sendPage(commandSender, books, page),
                e -> {
                    commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] There's an SQL error! (Check Console)");
                    e.printStackTrace();
                });
        return true;
    }

    private static void sendPage(CommandSender commandSender, List<BookDescriptions> books, int index) {
        if(books.isEmpty()){
            commandSender.sendMessage(ChatColor.of("RED") + "There are no books on the database!");
            return;
        }

        commandSender.sendMessage("List of set of 10 books number " + index);
        commandSender.sendMessage(ChatColor.of("GOLD") + "===============================================");
        if(10 * (index-1) > books.size()){
            commandSender.sendMessage(ChatColor.of("RED") + "There are no books in this set");
        }
        int end = 10 * (index-1) + 10;
        for(int i = 10 * (index-1); i < books.size() && i < end; i++){
            commandSender.sendMessage(ChatColor.of("WHITE") + books.get(i).getName() + "(" + books.get(i).getId() + ")");
        }
        commandSender.sendMessage(ChatColor.of("GOLD") + "===============================================\nTo view more books, type /si listbooks " + (index + 1));
    }
}
