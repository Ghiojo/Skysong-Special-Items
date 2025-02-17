package org.skysongdev.skysongitems.commands.lore;

import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.events.ItemEditEvent;
import org.skysongdev.skysongitems.utils.TextParser;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RenameCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;
        getPlugin().getUpdater().UpdateLore(player);
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta buffer;
        StringBuilder bufferString = new StringBuilder();

        if(strings.length < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Please type in the item's name!"));
            return true;
        }

        buffer = item.getItemMeta();
        PersistentDataContainer bufferDataContainer = buffer.getPersistentDataContainer();

        for(String string : strings){
            bufferString.append(string).append(" ");
        }

        bufferDataContainer.set(Utils.nameKey, PersistentDataType.STRING, bufferString.toString().trim());

        bufferString.replace(0, bufferString.length(), TextParser.translateHexColorCodes("&#", "", bufferString.toString()));
        bufferString.replace(0, bufferString.length(), TextParser.translateColorCodes(bufferString.toString()));

        buffer.displayName(getPlugin().getMiniMessage().deserialize(bufferString.toString().trim()).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.byBoolean(false)));

        item.setItemMeta(buffer);
        player.updateInventory();

        Bukkit.getPluginManager().callEvent(new ItemEditEvent(player));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player player = (Player) commandSender;

        if(player.getInventory().getItemInMainHand().getType() == Material.AIR)
            return List.of("NO ITEM IN HAND");
        else{
            if(player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(Utils.nameKey))
                return List.of(player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().get(Utils.nameKey, PersistentDataType.STRING));
            else
                return List.of("");
        }
    }
}
