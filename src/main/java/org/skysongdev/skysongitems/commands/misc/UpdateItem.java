package org.skysongdev.skysongitems.commands.misc;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class UpdateItem implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer = item.getItemMeta();
        ItemData data = ItemData.from(buffer);
        List<String> itemLore = data.getLoreSections();

        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to update!"));
            return true;
        }

        for(Property property : Constants.properties){
            if(property instanceof PrintableProperty || property instanceof RenameProperty){
                continue;
            }
            String patternName = property.getName().toLowerCase().replace("+", "\\+");
            Pattern pattern = Pattern.compile("(" + patternName + ")");
            Matcher matcher = pattern.matcher(itemLore.get(1).toLowerCase());
            if(matcher.find()){
                // Matches on the stored text, so an id contained in another id counts as present.
                String propertyString = data.getPropertiesText();
                if(propertyString != null && propertyString.contains(property.getId())){
                    continue;
                }
                data.addProperty(property.getId());
            }
        }
        item.setItemMeta(buffer);
        player.updateInventory();
        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));
        return true;
    }
}
