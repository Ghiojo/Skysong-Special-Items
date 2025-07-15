package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SetItemModel implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length < 2){
            commandSender.sendMessage("Usage: /si setitemmodel <namespace> <value>");
            return true;
        }
        if(!(commandSender instanceof Player)){
            commandSender.sendMessage("This command can only be used by players!");
            return true;
        }
        Player player = (Player) commandSender;
        if(player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().isAir()) {
            player.sendMessage("You must hold an item in your main hand to use this command.");
            return true;
        }

        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();
        meta.setItemModel(new NamespacedKey(strings[0], strings[1]));
        player.getInventory().getItemInMainHand().setItemMeta(meta);

        player.sendMessage("Item model set successfully to " + strings[0] + ":" + strings[1]);

        return true;
    }
}
