package org.skysongdev.skysongitems.commands.lore.physicaldesc;

import org.bukkit.Material;
import org.skysongdev.skysongitems.utils.Constants;
import org.skysongdev.skysongitems.utils.TextParser;
import org.skysongdev.skysongitems.utils.ItemData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class AddPhysical implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        List<String> newItemLore = new ArrayList<String>();
        int mode;
        String[] input;

        if (item == null || item.getType() == Material.AIR) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        if (strings.length < 1) {
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please type in lore for the item!"));
            return true;
        }
        if(strings[0].equals("-n")) {
            mode = 2;
            input = Arrays.copyOfRange(strings, 1, strings.length);
        }
         else {
            mode = 1;
            input = strings;
        }

        buffer = item.getItemMeta();
        ItemData data = ItemData.from(buffer);
        newItemLore = TextParser.manipulateSection(data.getLoreSections(), input, 0, mode);
        data.setLoreSections(newItemLore);
        buffer.lore(TextParser.TranslateToLore(newItemLore, data.getDeletedLines()));

        item.setItemMeta(buffer);
        player.updateInventory();

        return true;
    }
}
