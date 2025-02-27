package org.skysongdev.skysongitems.commands.charges;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RemoveCharges implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        ItemMeta buffer = item.getItemMeta();
        PersistentDataContainer container = buffer.getPersistentDataContainer();
        if(container.has(Utils.chargesKey))
            container.remove(Utils.chargesKey);
        if(container.has(Utils.maxchargesKey))
            container.remove(Utils.maxchargesKey);
        if(container.has(Utils.propertiesKey)){
            ArrayList<String> currentProperties = new ArrayList<String>(Arrays.stream(container.get(Utils.propertiesKey, PersistentDataType.STRING).split(";")).toList());
            currentProperties.remove("charges");
            if(currentProperties.size() == 0)
                container.remove(Utils.propertiesKey);
            else
                container.set(Utils.propertiesKey, PersistentDataType.STRING, String.join(";", currentProperties));
        }

        item.setItemMeta(buffer);
        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));
        return true;

    }
}
