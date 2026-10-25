package org.skysongdev.skysongitems.commands.properties;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RemoveProperty implements TabExecutor {
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

        if(!Utils.isAProperty(strings[0])){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid property!"));
        }

        getPlugin().getUpdater().UpdateItem(player);
        ItemMeta meta = itemInMainHand.getItemMeta();

        ItemData data = ItemData.from(meta);

        if(!data.hasProperties()){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This item has no properties."));
            return true;
        }

        List<String> currentProperties = data.getProperties();

        if(!currentProperties.contains(strings[0])){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This item does not have this property."));
            return true;
        }

        currentProperties.remove(strings[0]);

        data.setProperties(currentProperties);

        itemInMainHand.setItemMeta(meta);
        player.updateInventory();

        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<green>Property removed successfully!"));

        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        ArrayList<String> properties = new ArrayList<>();
        Player player = (Player) commandSender;
        if(player.getInventory().getItemInMainHand().getType().isAir())
            return List.of("NO ITEM IN MAIN HAND");
        ItemMeta meta = player.getInventory().getItemInMainHand().getItemMeta();

        ItemData data = ItemData.from(meta);
        if(!data.hasProperties())
            return List.of();

        for(String property : data.getProperties()){
            if(property.toLowerCase().startsWith(strings[0].toLowerCase())){
                properties.add(property);
            }
        }
        return properties;
    }
}
