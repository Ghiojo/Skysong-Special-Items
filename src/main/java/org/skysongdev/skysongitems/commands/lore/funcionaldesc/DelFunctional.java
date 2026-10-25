package org.skysongdev.skysongitems.commands.lore.funcionaldesc;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.skysongdev.skysongitems.utils.TextParser;
import org.skysongdev.skysongitems.utils.ItemData;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class DelFunctional implements CommandExecutor {
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
        ItemData data = ItemData.from(buffer);
        data.toggleSectionDeleted(1);

        itemLore.addAll(data.getLoreSections());

        buffer.lore(TextParser.TranslateToLore(itemLore, data.getDeletedLines()));
        item.setItemMeta(buffer);
        return true;
    }
}
