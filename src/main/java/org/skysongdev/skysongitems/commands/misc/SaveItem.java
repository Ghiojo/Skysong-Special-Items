package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.SkysongItems;
import org.skysongdev.skysongitems.commands.lore.Updater;
import org.skysongdev.skysongitems.utils.SkysongItem;
import org.skysongdev.skysongitems.utils.SkysongSkullItem;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.Objects;

public class SaveItem implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(!commandSender.hasPermission("skysongitems.save")) {
            commandSender.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>You do not have permission to use this command."));
            return false;
        }
        if(!(commandSender instanceof Player))
            return false;
        Player player = (Player) commandSender;
        if(player.getInventory().getItemInMainHand() == null || player.getInventory().getItemInMainHand().getType().isAir()) {
            player.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Please hold an item in your main hand!"));
            return false;
        }
        if(!player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(Utils.versionKey) || !Objects.equals(player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(Utils.versionKey, PersistentDataType.STRING), Utils.ITEM_VERSION)){
            SkysongItems.getPlugin().getUpdater().UpdateItem(player);
        }
        SkysongItem item;
        if(player.getInventory().getItemInMainHand().getType() == Material.PLAYER_HEAD)
            item = new SkysongSkullItem(player.getInventory().getItemInMainHand());
        else
            item = new SkysongItem(player.getInventory().getItemInMainHand());

        Utils.GenerateItemFile(item);

        player.sendMessage(SkysongItems.getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Item saved successfully!"));

        SkysongItems.getPlugin().fixFilePaths();
        return true;
    }
}
