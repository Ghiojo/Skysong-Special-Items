package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.utils.Constants;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class ReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;

        if (!player.hasPermission("skysongitems.reload")) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>You don't have permission to use this command!"));
            return true;
        }
        getPlugin().reloadConfig();

        getPlugin().getPropertyRegistry().load(getPlugin().getConfig(), getPlugin().getLogger());
        getPlugin().getTagRegistry().load(getPlugin().getConfig());

        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<green>Config reloaded!"));
        // Reload item files
        SkysongItems.getPlugin().fixFilePaths();


        return true;
    }
}
