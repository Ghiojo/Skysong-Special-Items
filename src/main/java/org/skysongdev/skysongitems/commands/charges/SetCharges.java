package org.skysongdev.skysongitems.commands.charges;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetCharges implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please type in lore for the item!"));
            return true;
        }
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        int num;
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        try{
            num = Integer.parseInt(strings[0]);
        }catch (NumberFormatException e){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please type in a valid number!"));
            return true;
        }
        if(ItemEditor.Result.INVALID_AMOUNT.equals(getPlugin().getItemEditor().setCharges(item, num))){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + " <red>\"" + strings[0] + "\" is negative!"));
            return true;
        }
        player.updateInventory();
        return true;
    }
}
