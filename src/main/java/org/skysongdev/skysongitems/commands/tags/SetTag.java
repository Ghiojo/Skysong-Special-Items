package org.skysongdev.skysongitems.commands.tags;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetTag implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("This command can only be used by players."));
            return true;
        }
        Player player = (Player) commandSender;

        if(strings.length < 2){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Usage: /si tag set <tag> <value>"));
            return true;
        }

        getPlugin().getUpdater().UpdateItem(player);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand == null || itemInMainHand.getType().isAir()) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You must hold an item in your main hand to use this command."));
            return true;
        }

        ItemEditor.Result result = getPlugin().getItemEditor().forCommand(player).setTag(itemInMainHand, strings[0], strings[1]);
        if(!ItemEditor.Result.DONE.equals(result)){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + failureMessage(result)));
            return true;
        }
        getPlugin().syncPermissionItems(player);

        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<green>The tag "+ strings[0] +" set to " + strings[1] + " successfully!"));

        return true;
    }

    private static String failureMessage(ItemEditor.Result result) {
        return switch (result) {
            case NOT_A_NUMBER -> "Invalid value for tag! (Requires a Number)";
            case NOT_A_BOOLEAN -> "Invalid value for tag! (Requires True or False)";
            default -> "Invalid Tag!";
        };
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> tags = new ArrayList<>();
        Player player = (Player) commandSender;
        if(player.getInventory().getItemInMainHand().getType().isAir())
            return List.of("NO ITEM IN HAND");

        if(strings.length == 1){
            for(NamespacedKey tag : getPlugin().getTagRegistry().getKeys()){
                    tags.add(tag.getKey());
            }
            return tags.stream().filter(a -> a.startsWith(strings[0])).toList();
        }

        if(strings.length >= 2){
            return List.of();
        }

        return tags;
    }
}
