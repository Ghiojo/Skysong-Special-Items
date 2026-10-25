package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.Constants;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

/**
 * {@code /si update}: brings the held item up to the current item version and
 * redraws its lore. Properties are not touched; they are added with {@code /si prop}.
 */
public class UpdateItem implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);

        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to update!"));
            return true;
        }

        player.updateInventory();
        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));
        return true;
    }
}
