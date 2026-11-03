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

public class SetAddable implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        ItemEditor editor = getPlugin().getItemEditor().forCommand(player);
        if(ItemEditor.Result.NOT_CHARGED.equals(editor.toggleChargeAdding(item))){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This item doesn't use charges!"));
            return true;
        }

        if (editor.canAddCharges(item)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<light_gray>Item charge adding is now <green>enabled") );
        } else {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<light_gray>Item charge adding is now <red>disabled") );
        }
        return true;
    }
}
