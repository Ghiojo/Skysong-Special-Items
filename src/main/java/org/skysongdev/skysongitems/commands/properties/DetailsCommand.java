package org.skysongdev.skysongitems.commands.properties;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.Property;
import org.skysongdev.skysongitems.utils.Utils;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class DetailsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("This command can only be used by players."));
            return true;
        }
        Player player = (Player) commandSender;
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (itemInMainHand == null || itemInMainHand.getType().isAir()) {
            player.sendMessage("You must hold an item in your main hand to use this command.");
            return true;
        }

        ItemMeta meta = itemInMainHand.getItemMeta();
        if(meta == null){
            player.sendMessage("This item has no properties.");
            return true;
        }

        player.sendMessage(getPlugin().getMiniMessage().deserialize("<#864811>Item properties:"));

        if(meta.getPersistentDataContainer().has(Constants.propertiesKey)) {
            String[] currentProperties = meta.getPersistentDataContainer().get(Constants.propertiesKey, PersistentDataType.STRING).split(";");
            StringBuilder returnMessage = new StringBuilder();
            for (String property : currentProperties) {
                Property prop = Utils.getProperty(property);
                returnMessage.append("<gold><hover:show_text:\"<gold>").append(prop.getName()).append("<newline><gray><i>").append(prop.getType()).append("property<newline><newline><gray>").append(prop.getDescription()).append("\">").append(prop.getName()).append("</hover></gold>").append("<#864811>, ");
            }
            player.sendMessage(getPlugin().getMiniMessage().deserialize(returnMessage.toString()));
        }

        return true;
    }
}
