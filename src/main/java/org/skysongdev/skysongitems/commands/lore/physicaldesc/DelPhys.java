package org.skysongdev.skysongitems.commands.lore.physicaldesc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.skysongdev.skysongitems.utils.TextParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class DelPhys implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateLore(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        List<String> itemLore = new ArrayList<String>();
        String preChange;
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize("<red>Please hold the item you wish to lore!"));
            return true;
        }

        ItemMeta buffer = item.getItemMeta();
        assert buffer != null;
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();
        if (bufferDataContainer.has(TextParser.deletedKey, PersistentDataType.STRING)) {
            String bufferString = bufferDataContainer.get(TextParser.deletedKey, PersistentDataType.STRING);
            StringBuilder builder = new StringBuilder(bufferString);
            if (builder.charAt(0) == '0') {
                builder.setCharAt(0, '1');
            } else {
                builder.setCharAt(0, '0');
            }
            bufferDataContainer.set(TextParser.deletedKey, PersistentDataType.STRING, builder.toString());

        } else {
            bufferDataContainer.set(TextParser.deletedKey, PersistentDataType.STRING, "100");
        }

        itemLore.addAll(Arrays.asList(bufferDataContainer.get(Utils.loreKey, PersistentDataType.STRING).split(ChatColor.COLOR_CHAR + "")));

        buffer.lore(TextParser.TranslateToLore(itemLore, bufferDataContainer.get(TextParser.deletedKey, PersistentDataType.STRING)));
        item.setItemMeta(buffer);
        return true;
    }
}
