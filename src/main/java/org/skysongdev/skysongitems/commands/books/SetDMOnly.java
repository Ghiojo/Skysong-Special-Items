package org.skysongdev.skysongitems.commands.books;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.SkysongItems;

public class SetDMOnly implements CommandExecutor {
    public static NamespacedKey boundKey = new NamespacedKey(SkysongItems.getPlugin(), "bound_book");
    public static NamespacedKey dMOnlyKey = new NamespacedKey(SkysongItems.getPlugin(), "dm_only");
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        try {
            item.getItemMeta().getLore();
        } catch (Exception e) {
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] Please hold an item in your hand!");
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(boundKey, PersistentDataType.INTEGER)) {
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] This item has no book bound to it!");
            return true;
        }

        if (container.has(dMOnlyKey, PersistentDataType.BOOLEAN)) {
            if (container.get(dMOnlyKey, PersistentDataType.BOOLEAN)) {
                container.set(dMOnlyKey, PersistentDataType.BOOLEAN, false);
                player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "DM only mode has been " + ChatColor.of("RED") + "disabled");
            } else {
                container.set(dMOnlyKey, PersistentDataType.BOOLEAN, true);
                player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "DM only mode has been " + ChatColor.of("GREEN") + "enabled");
            }
        } else {
            container.set(dMOnlyKey, PersistentDataType.BOOLEAN, true);
            player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "DM only mode has been " + ChatColor.of("GREEN") + "enabled");
        }
        item.setItemMeta(meta);

        return true;
    }
}
