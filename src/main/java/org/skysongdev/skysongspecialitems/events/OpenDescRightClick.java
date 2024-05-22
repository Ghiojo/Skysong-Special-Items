package org.skysongdev.skysongspecialitems.events;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongspecialitems.SkysongSpecialItems;
import org.skysongdev.skysongspecialitems.database.BookDescriptions;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class OpenDescRightClick implements Listener {
    SkysongSpecialItems plugin;
    public static NamespacedKey rightClickKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "is_right_clickable");
    public static NamespacedKey dMOnlyKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "dm_only");
    public static NamespacedKey boundKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "bound_book");

    public OpenDescRightClick(SkysongSpecialItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemUse(PlayerInteractEvent event){
        ItemStack item = event.getItem();
        Action action = event.getAction();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        if(action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK){
            return;
        }
        if (item.getItemMeta().getPersistentDataContainer().has(rightClickKey)) {
            if (item.getItemMeta().getPersistentDataContainer().get(rightClickKey, PersistentDataType.BOOLEAN)){
                int idToOpen = item.getItemMeta().getPersistentDataContainer().get(boundKey, PersistentDataType.INTEGER);
                BookDescriptions book;
                try {
                    book = plugin.getDatabase().findBookByID(idToOpen);
                    if(book == null){
                        event.getPlayer().sendMessage(ChatColor.of("RED") + "[ERROR] Couldn't find a book with this id!");
                        return;
                    }
                }catch (SQLException e){
                    event.getPlayer().sendMessage(ChatColor.of("RED") + "[ERROR] There's an SQL error! (Check Console)");
                    e.printStackTrace();
                    return;
                }

                if(item.getItemMeta().getPersistentDataContainer().has(dMOnlyKey, PersistentDataType.BOOLEAN) && item.getItemMeta().getPersistentDataContainer().get(dMOnlyKey, PersistentDataType.BOOLEAN)){
                    if(!event.getPlayer().hasPermission("skysongspecialitems.dm")){
                        event.getPlayer().sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("RED").toString() + "You do not have permission to view this lore");
                        return;
                    }
                }

                ItemStack tempBook = new ItemStack(Material.WRITTEN_BOOK);
                BookMeta tempMeta = (BookMeta) tempBook.getItemMeta();
                List<String> pages = Arrays.asList(book.getBookcontent().split("\\[PageEnd\\]"));
                tempMeta.setPages(pages);
                tempMeta.setTitle(book.getName());
                tempMeta.setAuthor("Skysong");
                tempBook.setItemMeta(tempMeta);

                event.getPlayer().openBook(tempBook);
            }
        }
    }

}
