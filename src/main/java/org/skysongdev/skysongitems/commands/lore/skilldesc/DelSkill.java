package org.skysongdev.skysongitems.commands.lore.skilldesc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.TextParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class DelSkill implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        List<String> itemLore = new ArrayList<String>();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("<red>Please hold the item you wish to lore!"));
            return true;
        }

        ItemMeta buffer = item.getItemMeta();
        assert buffer != null;
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();
        if (bufferDataContainer.has(Constants.deletedKey, PersistentDataType.STRING)) {
            String bufferString = bufferDataContainer.get(Constants.deletedKey, PersistentDataType.STRING);
            StringBuilder builder = new StringBuilder(bufferString);
            if (builder.charAt(2) == '0') {
                builder.setCharAt(2, '1');
            } else {
                builder.setCharAt(2, '0');
            }
            bufferDataContainer.set(Constants.deletedKey, PersistentDataType.STRING, builder.toString());

        } else {
            bufferDataContainer.set(Constants.deletedKey, PersistentDataType.STRING, "001");
        }

        itemLore.addAll(Arrays.asList(bufferDataContainer.get(Constants.loreKey, PersistentDataType.STRING).split(ChatColor.COLOR_CHAR + "")));

        buffer.lore(TextParser.TranslateToLore(itemLore, bufferDataContainer.get(Constants.deletedKey, PersistentDataType.STRING)));
        item.setItemMeta(buffer);
        return true;
    }
}
