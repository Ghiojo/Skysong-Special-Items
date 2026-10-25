package org.skysongdev.skysongitems.listeners;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.database.BookDescriptions;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import org.skysongdev.skysongitems.utils.ItemKeys;

public class OpenDescRightClick implements Listener {
    SkysongItems plugin;

    public OpenDescRightClick(SkysongItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemUse(PlayerInteractEvent event){
        OptionalInt idToOpen = boundBookToOpen(event);
        if (idToOpen.isEmpty()) {
            return;
        }
        BookDescriptions book;
        try {
            book = plugin.getBookRepository().findById(idToOpen.getAsInt());
            if(book == null){
                event.getPlayer().sendMessage(ChatColor.of("RED") + "[ERROR] Couldn't find a book with this id!");
                return;
            }
        }catch (SQLException e){
            event.getPlayer().sendMessage(ChatColor.of("RED") + "[ERROR] There's an SQL error! (Check Console)");
            e.printStackTrace();
            return;
        }

        if(isDmOnly(event.getItem()) && !event.getPlayer().hasPermission("skysongspecialitems.dm")){
            event.getPlayer().sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("RED").toString() + "You do not have permission to view this lore");
            return;
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

    /**
     * The id of the book this right-click should open, or empty when it should
     * open nothing: not a right-click, an item without the right-click flag, an
     * item with no bound book, or an off-hand event whose main hand already
     * opens a book.
     */
    OptionalInt boundBookToOpen(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return OptionalInt.empty();
        }
        ItemStack item = event.getItem();
        if (!isRightClickable(item) || isHandledByMainHand(event)) {
            return OptionalInt.empty();
        }
        Integer id = item.getItemMeta().getPersistentDataContainer().get(ItemKeys.BOUND_BOOK, PersistentDataType.INTEGER);
        return id == null ? OptionalInt.empty() : OptionalInt.of(id);
    }

    /**
     * Bukkit fires one interact event per hand. When the main hand holds a
     * right-clickable item, the main-hand event opens its book, so the
     * off-hand event must not open a second one.
     */
    private static boolean isHandledByMainHand(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return false;
        }
        Player player = event.getPlayer();
        return isRightClickable(player.getInventory().getItemInMainHand());
    }

    private static boolean isRightClickable(ItemStack item) {
        return hasTrueFlag(item, ItemKeys.RIGHT_CLICKABLE);
    }

    private static boolean isDmOnly(ItemStack item) {
        return hasTrueFlag(item, ItemKeys.DM_ONLY);
    }

    private static boolean hasTrueFlag(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        return Boolean.TRUE.equals(container.get(key, PersistentDataType.BOOLEAN));
    }
}
