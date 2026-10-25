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
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.ItemKeys;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetCharges implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please type in lore for the item!"));
            return true;
        }
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        int num;
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        try{
            num = Integer.parseInt(strings[0]);
        }catch (NumberFormatException e){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please type in a valid number!"));
            return true;
        }
        if(num < 0){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + " <red>\"" + strings[0] + "\" is negative!"));
            return true;
        }
        ItemMeta buffer = item.getItemMeta();
        PersistentDataContainer container = buffer.getPersistentDataContainer();
        if(!container.has(ItemKeys.MAX_CHARGES)) {
            container.set(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER, num);
            if(container.has(ItemKeys.PROPERTIES)){
                String properties = container.get(ItemKeys.PROPERTIES, PersistentDataType.STRING);
                if(!properties.contains("charges"))
                    container.set(ItemKeys.PROPERTIES, PersistentDataType.STRING, properties + ";charges");
            }
            else
                container.set(ItemKeys.PROPERTIES, PersistentDataType.STRING, "charges");
        }
        if(num > container.get(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER))
            num = container.get(ItemKeys.MAX_CHARGES, PersistentDataType.INTEGER);
        container.set(ItemKeys.CHARGES, PersistentDataType.INTEGER, num);

        item.setItemMeta(buffer);
        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));
        return true;
    }
}
