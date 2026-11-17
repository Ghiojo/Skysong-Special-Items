package org.skysongdev.skysongitems.commands.charges;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skysongdev.skysongitems.commands.timer.LaterEdits;
import org.skysongdev.skysongitems.commands.timer.TimingArgs;
import org.skysongdev.skysongitems.timer.action.BuiltInTimerActions;
import org.skysongdev.skysongitems.utils.Constants;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RemoveCharges implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        TimingArgs timing = TimingArgs.readOrRefuse(player, strings, 0, true);
        if (timing == null) {
            return true;
        }
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        if (timing.isLater()) {
            LaterEdits.schedule(player, item, timing, BuiltInTimerActions.REMOVE_CHARGES, null, null);
        } else {
            getPlugin().getItemEditor().forCommand(player).removeCharges(item);
        }
        player.updateInventory();
        return true;
    }
}
