package org.skysongdev.skysongitems.commands.custommodeldata;

import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class ListCustomModelData implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Usage: /addcmd <custom_model_data_id> <value>"));
            return true;
        }
        Player player = (Player) commandSender;

        if(player.getInventory().getItemInMainHand().getType() == org.bukkit.Material.AIR){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>You must be holding an item to use this command!"));
            return true;
        }
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemMeta meta = itemInHand.getItemMeta();

        if(!meta.hasCustomModelDataComponent()){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>This item does not have custom model data!"));
            return true;
        }

        switch(strings[0].toLowerCase()){
            case "strings":
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Custom Model Data Strings: <yellow>"));
                List<String> cmdStrings = meta.getCustomModelDataComponent().getStrings();
                for(int i = 0; i < cmdStrings.size(); i++){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<gold>" + (i + 1) + ") <yellow>" + cmdStrings.get(i)));
                }
                break;
            case "floats":
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Custom Model Data Floats: <yellow>"));
                List<Float> cmdFloats = meta.getCustomModelDataComponent().getFloats();
                for(int i = 0; i < cmdFloats.size(); i++){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<gold>" + (i + 1) + ") <yellow>" + cmdFloats.get(i)));
                }
                break;
            case "flags":
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Custom Model Data Flags: <yellow>"));
                List<Boolean> cmdFlags = meta.getCustomModelDataComponent().getFlags();
                for(int i = 0; i < cmdFlags.size(); i++){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<gold>" + (i + 1) + ") <yellow>" + cmdFlags.get(i)));
                }
                break;
            case "colors":
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Custom Model Data Colors: <yellow>"));
                List<Color> cmdColors = meta.getCustomModelDataComponent().getColors();
                for(int i = 0; i < cmdColors.size(); i++){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<gold>" + (i + 1) + ") <yellow>" + cmdColors.get(i)));
                }
                break;
            default:
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid custom model data type provided! Valid types are: strings, floats, flags, colors."));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of("strings", "floats", "flags", "colors");
    }
}
