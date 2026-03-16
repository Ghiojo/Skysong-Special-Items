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
import org.skysongdev.skysongitems.utils.Utils;

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

        if(!(Utils.isATag(strings[0]))){
            player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid Tag!"));
            return true;
        }

        PersistentDataType<?, ?> tagValue = Constants.tagList.get(Utils.getTag(strings[0]));

        ItemMeta meta = itemInMainHand.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();

        if(tagValue == PersistentDataType.INTEGER){
            try{
                Integer.parseInt(strings[1]);
            } catch (NumberFormatException e){
                player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid value for tag! (Requires a Number)"));
                return true;
            }
            container.set(Utils.getTag(strings[0]), Constants.tagList.get(Utils.getTag(strings[0])), Integer.parseInt(strings[1]));
        } else if(tagValue == PersistentDataType.BOOLEAN){
            if(!(strings[1].equalsIgnoreCase("true") || strings[1].equalsIgnoreCase("false"))){
                player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid value for tag! (Requires True or False)"));
                return true;
            }
            container.set(Utils.getTag(strings[0]), Constants.tagList.get(Utils.getTag(strings[0])), strings[1].equalsIgnoreCase("true"));
        }  else if (tagValue == PersistentDataType.FLOAT){
            try{
                Float.parseFloat(strings[1]);
            } catch (NumberFormatException e){
                player.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "Invalid value for tag! (Requires a Number)"));
                return true;
            }
            container.set(Utils.getTag(strings[0]), Constants.tagList.get(Utils.getTag(strings[0])), Float.parseFloat(strings[1]));
        } else if (tagValue == PersistentDataType.STRING){
            if(strings[0].equals("permission")){
                if(container.has(Utils.getTag("permission"), PersistentDataType.STRING)){
                    Utils.updatePlayerPermissions(player, false, container.get(Utils.getTag("permission"), PersistentDataType.STRING));
                }
                Utils.updatePlayerPermissions(player, true, strings[1]);
            }
            container.set(Utils.getTag(strings[0]), Constants.tagList.get(Utils.getTag(strings[0])), strings[1]);
        }

        itemInMainHand.setItemMeta(meta);

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
            for(NamespacedKey tag : Constants.tagList.keySet()){
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
