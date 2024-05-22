package org.skysongdev.skysongspecialitems.commands;

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
import org.skysongdev.skysongspecialitems.SkysongSpecialItems;

public class DisableFunc implements CommandExecutor {
    public static NamespacedKey disabledKey = new NamespacedKey(SkysongSpecialItems.getPlugin(), "is_disabled");
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        try{
            item.getItemMeta();
        }catch(Exception e){
            commandSender.sendMessage(ChatColor.of("RED").toString() + "[ERROR] Please hold an item!");
            return true;
        }
        ItemMeta buffer = item.getItemMeta();
        assert buffer != null;
        PersistentDataContainer dataContainer = buffer.getPersistentDataContainer();
        if (dataContainer.has(disabledKey, PersistentDataType.BOOLEAN)) {
            if(dataContainer.get(disabledKey, PersistentDataType.BOOLEAN)){
                dataContainer.set(disabledKey, PersistentDataType.BOOLEAN, false);
                player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Item functions have been " + ChatColor.of("GREEN") + "enabled");
            }
            else{
                dataContainer.set(disabledKey, PersistentDataType.BOOLEAN, true);
                player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Item functions have been " + ChatColor.of("RED") + "disabled");
            }

        } else {
            dataContainer.set(disabledKey, PersistentDataType.BOOLEAN, true);
            player.sendMessage(ChatColor.of("DARK_GRAY") + "[" + ChatColor.of("GOLD") + "SkysongItems" + ChatColor.of("DARK_GRAY") + "] " + ChatColor.of("GRAY") + "Item functions have been " + ChatColor.of("RED") + "disabled");
        }
        item.setItemMeta(buffer);

        return true;
    }
}
