package org.skysongdev.skysongitems.commands.tags;

import org.bukkit.NamespacedKey;
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
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.TagRegistry;

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

        TagRegistry tagRegistry = getPlugin().getTagRegistry();
        if(!(tagRegistry.contains(strings[0]))){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid Tag!"));
            return true;
        }

        NamespacedKey tag = tagRegistry.getKey(strings[0]);
        PersistentDataType<?, ?> tagValue = tagRegistry.getType(tag);

        ItemMeta meta = itemInMainHand.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if(PersistentDataType.INTEGER.equals(tagValue)){
            try{
                Integer.parseInt(strings[1]);
            } catch (NumberFormatException e){
                player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid value for tag! (Requires a Number)"));
                return true;
            }
            container.set(tag, PersistentDataType.INTEGER, Integer.parseInt(strings[1]));
        } else if(PersistentDataType.BOOLEAN.equals(tagValue)){
            if(!(strings[1].equalsIgnoreCase("true") || strings[1].equalsIgnoreCase("false"))){
                player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid value for tag! (Requires True or False)"));
                return true;
            }
            container.set(tag, PersistentDataType.BOOLEAN, strings[1].equalsIgnoreCase("true"));
        }  else if (PersistentDataType.FLOAT.equals(tagValue)){
            try{
                Float.parseFloat(strings[1]);
            } catch (NumberFormatException e){
                player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid value for tag! (Requires a Number)"));
                return true;
            }
            container.set(tag, PersistentDataType.FLOAT, Float.parseFloat(strings[1]));
        } else if (PersistentDataType.STRING.equals(tagValue)){
            container.set(tag, PersistentDataType.STRING, strings[1]);
        }

        itemInMainHand.setItemMeta(meta);
        getPlugin().syncPermissionItems(player);

        player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<green>The tag "+ strings[0] +" set to " + strings[1] + " successfully!"));

        return true;
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
