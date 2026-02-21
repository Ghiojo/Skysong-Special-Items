package org.skysongdev.skysongitems.commands.custommodeldata;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class RemoveCustomModelData implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length < 2){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Usage: /addcmd <custom_model_data_id> <value> <optional: true/false/t/f> (only needed if you want to delete the position instead of just setting it to an empty value)"));
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

        int customModelDataIndex;
        try {
            customModelDataIndex = Integer.parseInt(strings[1]);
        } catch (NumberFormatException e){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Please enter a numeric value."));
            return true;
        }
        if(customModelDataIndex < 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Custom model data index must be 1 or higher."));
            return true;
        }

        boolean deletePosition = false;
        if(strings.length == 3){
            if(strings[2].toLowerCase() == "true" || strings[2].toLowerCase() == "false" || strings[2].toLowerCase() == "t" || strings[2].toLowerCase() == "f"){
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. If you plan on deleting the position you must put 'true' or 'false' as the value"));
                return true;
            } else {
                if(strings[2].toLowerCase() == "true" || strings[2].toLowerCase() == "t"){
                    deletePosition = true;
                }
            }
        }


        switch(strings[0].toLowerCase()){
            case "strings":
                ArrayList<String> stringList = new ArrayList<>(meta.getCustomModelDataComponent().getStrings());
                if(customModelDataIndex > stringList.size()){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Custom model data index is out of bounds."));
                    return true;
                }
                if(deletePosition){
                    stringList.remove(customModelDataIndex - 1);
                } else {
                    stringList.set(customModelDataIndex - 1, "");
                }
                meta.getCustomModelDataComponent().setStrings(stringList);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully removed string custom model data with index <yellow>" + customModelDataIndex + " <green>from the item!"));
                break;
            case "floats":
                ArrayList<Float> floatList = new ArrayList<>(meta.getCustomModelDataComponent().getFloats());
                if(customModelDataIndex > floatList.size()){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Custom model data index is out of bounds."));
                    return true;
                }
                if(deletePosition){
                    floatList.remove(customModelDataIndex - 1);
                } else {
                    floatList.set(customModelDataIndex - 1, 0f);
                }
                meta.getCustomModelDataComponent().setFloats(floatList);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully removed float custom model data with index <yellow>" + customModelDataIndex + " <green>from the item!"));
                break;
            case "flags":
                ArrayList<Boolean> flagList = new ArrayList<>(meta.getCustomModelDataComponent().getFlags());
                if(customModelDataIndex > flagList.size()){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Custom model data index is out of bounds."));
                    return true;
                }
                if(deletePosition){
                    flagList.remove(customModelDataIndex - 1);
                } else {
                    flagList.set(customModelDataIndex - 1, false);
                }
                meta.getCustomModelDataComponent().setFlags(flagList);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully removed flag custom model data with index <yellow>" + customModelDataIndex + " <green>from the item!"));
                break;
            case "colors":
                ArrayList<org.bukkit.Color> colorList = new ArrayList<>(meta.getCustomModelDataComponent().getColors());
                if(customModelDataIndex > colorList.size()){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Custom model data index is out of bounds."));
                    return true;
                }
                if(deletePosition){
                    colorList.remove(customModelDataIndex - 1);
                } else {
                    colorList.set(customModelDataIndex - 1, org.bukkit.Color.fromRGB(0, 0, 0));
                }
                meta.getCustomModelDataComponent().setColors(colorList);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully removed color custom model data with index <yellow>" + customModelDataIndex + " <green>from the item!"));
                break;
            default:
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid custom model data type. Valid types are: strings, floats, flags, colors."));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of();
    }
}
