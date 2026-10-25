package org.skysongdev.skysongitems.commands.lore.skilldesc;

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
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetSkill implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateItem(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        List<String> newItemLore = new ArrayList<String>();
        if(item == null || item.getType() == Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please hold the item you wish to lore!"));
            return true;
        }
        if(strings.length < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Constants.pluginTag + "<red>Please type in lore for the item!"));
            return true;
        }

        buffer = item.getItemMeta();
        ItemData data = ItemData.from(buffer);
        newItemLore = TextParser.manipulateSection(data.getLoreSections(), strings, 2, 0);
        data.setLoreSections(newItemLore);
        buffer.lore(TextParser.TranslateToLore(newItemLore, data.getDeletedLines()));

        item.setItemMeta(buffer);
        player.updateInventory();

        return true;
    }
}
