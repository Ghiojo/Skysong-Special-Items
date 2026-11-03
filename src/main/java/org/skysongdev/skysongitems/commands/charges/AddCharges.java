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

public class AddCharges implements CommandExecutor {
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
        boolean ignoreLock = player.hasPermission("skysongspecialitems.cit");
        ItemEditor.Result result = getPlugin().getItemEditor().addCharges(item, num, ignoreLock);
        if(ItemEditor.Result.NOT_CHARGED.equals(result)){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This item doesn't use charges!"));
        } else if(ItemEditor.Result.ADDING_LOCKED.equals(result)){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>You cannot add charges to this item!"));
        } else {
            player.updateInventory();
        }
        return true;
    }
}
