package org.skysongdev.skysongitems.commands.lore.misc;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.utils.PrintableProperty;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.RenameProperty;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class UpdateItem implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer = item.getItemMeta();
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();
        ArrayList<String> itemLore = new ArrayList<String>(Arrays.stream(bufferDataContainer.get(Utils.loreKey, PersistentDataType.STRING).split(ChatColor.COLOR_CHAR + "")).toList());

        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Please hold the item you wish to update!"));
            return true;
        }

        getPlugin().getUpdater().UpdateLore(player);

        for(Property property : Utils.properties){
            if(property instanceof PrintableProperty || property instanceof RenameProperty){
                continue;
            }
            Pattern pattern = Pattern.compile("(" + property.getName().toLowerCase() + ")");
            Matcher matcher = pattern.matcher(itemLore.get(1).toLowerCase());
            if(matcher.find()){
                String propertyString = bufferDataContainer.get(Utils.propertiesKey, PersistentDataType.STRING);
                if(propertyString != null && propertyString.contains(property.getId())){
                    continue;
                }
                if(propertyString == null){
                    propertyString = property.getId();
                } else {
                    propertyString = propertyString + ";" + property.getId();
                }
                bufferDataContainer.set(Utils.propertiesKey, PersistentDataType.STRING, propertyString);
            }
        }
        return true;
    }
}
