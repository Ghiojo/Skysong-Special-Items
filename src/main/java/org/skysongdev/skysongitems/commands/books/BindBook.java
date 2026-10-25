package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.database.BookQueries;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;

public class BindBook implements CommandExecutor {
    private final BookQueries bookQueries;

    public BindBook(BookQueries bookQueries) {
        this.bookQueries = bookQueries;
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

        // The item is the one held when the command was sent, even if the player switches items before the lookup ends.
        bookQueries.submit(books -> books.findById(id) != null,
                found -> bindIfFound(player, item, id, found),
                e -> {
                    commandSender.sendMessage(ChatColor.of("RED") + "[ERROR] There's an SQL error! (Check Console)");
                    e.printStackTrace();
                });
        return true;
    }

    private static void bindIfFound(Player player, ItemStack item, int id, boolean found) {
        if(!player.isOnline()){
            return;
        }
        if(!found){
            player.sendMessage(ChatColor.of("RED") + "[ERROR] Couldn't find a book with this id!");
            return;
        }
        ItemMeta buffer = item.getItemMeta();
        PersistentDataContainer itemDataContainer = buffer.getPersistentDataContainer();
        itemDataContainer.set(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER, id);
        item.setItemMeta(buffer);
        player.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<green>Book with id " + id + " bound successfully!"));
    }
}
