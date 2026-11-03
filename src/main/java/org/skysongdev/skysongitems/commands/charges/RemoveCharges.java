package org.skysongdev.skysongitems.commands.charges;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.utils.Constants;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RemoveCharges implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        getPlugin().getItemEditor().removeCharges(item);
        player.updateInventory();
        return true;
    }
}
