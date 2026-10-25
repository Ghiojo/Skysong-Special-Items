package org.skysongdev.skysongitems.commands.properties;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.skysongdev.skysongitems.utils.ItemKeys;

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

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if(!(container.has(ItemKeys.PROPERTIES))){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This item has no properties."));
            return true;
        }

        ArrayList<String> currentProperties = new ArrayList<String>(Arrays.stream(container.get(ItemKeys.PROPERTIES, PersistentDataType.STRING).split(";")).toList());

        if(!currentProperties.contains(strings[0])){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>This item does not have this property."));
            return true;
        }

        currentProperties.remove(strings[0]);

        container.set(ItemKeys.PROPERTIES, PersistentDataType.STRING, String.join(";", currentProperties));

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

        if(!(meta.getPersistentDataContainer().has(ItemKeys.PROPERTIES)))
            return List.of();

        for(String property : meta.getPersistentDataContainer().get(ItemKeys.PROPERTIES, PersistentDataType.STRING).split(";")){
            if(property.toLowerCase().startsWith(strings[0].toLowerCase())){
                properties.add(property);
            }
        }
        return properties;
    }
}
