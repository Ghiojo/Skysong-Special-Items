package org.skysongdev.skysongitems.commands.charges;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetAddable implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if(!container.has(ItemKeys.MAX_CHARGES) || !container.has(ItemKeys.CHARGES)){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This item doesn't use charges!"));
            return true;
        }

        if (container.has(ItemKeys.CAN_ADD_CHARGES) && container.get(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN)) {
            container.set(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN, false);
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<light_gray>Item charge adding is now <red>disabled") );
        } else {
            container.set(ItemKeys.CAN_ADD_CHARGES, PersistentDataType.BOOLEAN, true);
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<light_gray>Item charge adding is now <green>enabled") );
        }
        item.setItemMeta(meta);

        return true;
    }
}
