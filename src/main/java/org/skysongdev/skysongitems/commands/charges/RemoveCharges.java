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
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.Constants;

import org.skysongdev.skysongitems.utils.ItemData;
import org.skysongdev.skysongitems.utils.ItemKeys;
import org.skysongdev.skysongitems.utils.ItemLoreRenderer;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RemoveCharges implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        ItemMeta buffer = item.getItemMeta();
        PersistentDataContainer container = buffer.getPersistentDataContainer();
        if(container.has(ItemKeys.CHARGES))
            container.remove(ItemKeys.CHARGES);
        if(container.has(ItemKeys.MAX_CHARGES))
            container.remove(ItemKeys.MAX_CHARGES);
        ItemData data = new ItemData(container);
        // The lore refresh after every edit skips items without properties,
        // so clear the charges line here when charges was the last property.
        if(data.removeProperty("charges") && !data.hasProperties())
            ItemLoreRenderer.renderWithoutProperties(buffer);

        item.setItemMeta(buffer);
        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));
        return true;

    }
}
