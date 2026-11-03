package org.skysongdev.skysongitems.commands.tags;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemEditor;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RemoveTag implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("This command can only be used by players."));
            return true;
        }
        Player player = (Player) commandSender;

        if(strings.length < 1){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Usage: /properties add <property>"));
            return true;
        }

        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand == null || itemInMainHand.getType().isAir()) {
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "You must hold an item in your main hand to use this command."));
            return true;
        }

        if(!getPlugin().getTagRegistry().contains(strings[0])){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid Tag!"));
            return true;
        }

        // The updater may replace the held item, so edit the one it leaves in the hand.
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack updatedItem = player.getInventory().getItemInMainHand();

        if(ItemEditor.Result.MISSING.equals(getPlugin().getItemEditor().forCommand(player).removeTag(updatedItem, strings[0]))){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This item does not have this tag."));
            return true;
        }
        getPlugin().syncPermissionItems(player);

        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<green>Tag removed successfully!"));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> tags = new ArrayList<>();
        Player player = (Player) commandSender;
        if(player.getInventory().getItemInMainHand().getType().isAir())
            return List.of("NO ITEM IN MAIN HAND");
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

        for(NamespacedKey tag : meta.getPersistentDataContainer().getKeys()){
            if(!getPlugin().getTagRegistry().contains(tag)){
                continue;
            }
            if(tag.getKey().startsWith(strings[0].toLowerCase())){
                tags.add(tag.getKey());
            }
        }
        return tags;
    }
}
