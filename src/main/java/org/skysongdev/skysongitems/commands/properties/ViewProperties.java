package org.skysongdev.skysongitems.commands.properties;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.Property;

import java.util.List;
import java.util.Objects;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class ViewProperties implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        String proptype;
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        if(strings.length < 1){
            proptype = "all";
        } else
            proptype = strings[0];

        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("<red>Please hold the item you wish to lore!"));
            return true;
        }

        buffer = item.getItemMeta();
        ItemData data = ItemData.from(buffer);

        if(!data.hasProperties()){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("<red>This item has no properties!"));
            return true;
        }

        if(!(getPlugin().getPropertyRegistry().getPropertyTypes().contains(proptype)) && !(proptype.equalsIgnoreCase("all"))){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("<red>Invalid property type!"));
            return true;
        }

        StringBuilder bufferString = new StringBuilder();

        if(proptype.equalsIgnoreCase("all")){
            for(String prop : getPlugin().getPropertyRegistry().getPropertyTypes()){
                for(String string : data.getProperties()){
                    if(!Objects.equals(getPlugin().getPropertyRegistry().get(string).getPropType(), prop)){
                        continue;
                    }
                    Property property = getPlugin().getPropertyRegistry().get(string);
                    bufferString.append("<hover:show_text:\"<gray>").append(property.getDescription()).append("\"><gray>").append(property.getName()).append("</hover>, ");
                }
                if(bufferString.length() > 0) {
                    bufferString.replace(0, bufferString.length(), bufferString.substring(0, bufferString.length() - 2)) ;
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("<gold><u><b>" + prop));
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(bufferString.toString()));
                    bufferString.replace(0, bufferString.length(), "");
                }
            }
        } else {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("<gold><u><b>" + proptype));
            for(String string : data.getProperties()){
                if(!Objects.equals(getPlugin().getPropertyRegistry().get(string).getPropType(), proptype)){
                    continue;
                }
                Property property = getPlugin().getPropertyRegistry().get(string);
                bufferString.append("<hover:show_text:\"<gray>").append(property.getDescription()).append("\"><gray>").append(property.getName()).append("<reset>, ");
                bufferString.replace(0, bufferString.length(), bufferString.substring(0, bufferString.length() - 2)) ;
            }
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(bufferString.toString()));
            bufferString.replace(0, bufferString.length(), "");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return getPlugin().getPropertyRegistry().getPropertyTypes().stream().filter(a -> a.startsWith(strings[0])).toList();
    }
}
