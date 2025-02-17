package org.skysongdev.skysongitems.commands.charges;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.utils.Utils;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetAddable implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if(!container.has(Utils.maxchargesKey) || !container.has(Utils.chargesKey)){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>This item doesn't use charges!"));
            return true;
        }

        if (container.has(Utils.addablechargesKey) && container.get(Utils.addablechargesKey, PersistentDataType.BOOLEAN)) {
            container.set(Utils.addablechargesKey, PersistentDataType.BOOLEAN, false);
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<light_gray>Item charge adding is now <red>disabled") );
        } else {
            container.set(Utils.addablechargesKey, PersistentDataType.BOOLEAN, true);
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<light_gray>Item charge adding is now <green>enabled") );
        }
        item.setItemMeta(meta);

        return true;
    }
}
