package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.SkysongItem;
import org.skysongdev.skysongitems.utils.Utils;

import java.io.File;
import java.util.List;

public class LoadItem implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(s.isEmpty()){
            commandSender.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Usage: /loaditem <item_name>"));
            return true;
        }
        if(!commandSender.hasPermission("skysongitems.load")) {
            commandSender.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>You do not have permission to use this command."));
            return true;
        }
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This command can only be used by players!"));
            return true;
        }
        Player player = (Player) commandSender;

        if(!Constants.itemFileNames.contains(strings[0])) {
            player.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Item not found!"));
            return true;
        }
        if(player.getInventory().firstEmpty() == -1) {
            player.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Your inventory is full!"));
            return true;
        }
        File itemFile;
        try{
            itemFile = new File(Utils.getOrGenerateOutputFile(), strings[0] + ".yml");
        }catch (Exception e) {
            player.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Error loading item: " + e.getMessage()));
            return true;
        }

        SkysongItem item = Utils.LoadSkysongItem(itemFile);

        player.getInventory().addItem(SkysongItems.getPlugin().getUtils().GenerateSkysongItem(item));

        player.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<green>Item loaded successfully!"));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return Constants.itemFileNames.stream()
                .filter(itemName -> itemName.startsWith(strings[0]))
                .toList();
    }
}
