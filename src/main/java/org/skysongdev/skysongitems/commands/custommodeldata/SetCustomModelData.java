package org.skysongdev.skysongitems.commands.custommodeldata;

import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skysongdev.skysongitems.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class SetCustomModelData implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length != 3){
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

        CustomModelDataComponent cmdComponent = meta.getCustomModelDataComponent();

        switch(strings[0].toLowerCase()){
            case "strings":
                ArrayList<String> stringList = new ArrayList<>(meta.getCustomModelDataComponent().getStrings());
                if(customModelDataIndex > stringList.size()){
                    for(int i = stringList.size(); i < customModelDataIndex; i++){
                        stringList.add("");
                    }
                }
                stringList.set(customModelDataIndex - 1, strings[2]);
                cmdComponent.setStrings(stringList);
                meta.setCustomModelDataComponent(cmdComponent);
                itemInHand.setItemMeta(meta);
                break;
            case "flags":
                ArrayList<Boolean> flagList = new ArrayList<>(meta.getCustomModelDataComponent().getFlags());
                try{
                    Boolean.parseBoolean(strings[2]);
                } catch (Exception e){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Please enter a boolean value (true/false)."));
                    return true;
                }
                if(customModelDataIndex > flagList.size()){
                    for(int i = flagList.size(); i < customModelDataIndex; i++){
                        flagList.add(false);
                    }
                }
                flagList.set(customModelDataIndex - 1, Boolean.parseBoolean(strings[2]));
                cmdComponent.setFlags(flagList);
                meta.setCustomModelDataComponent(cmdComponent);
                itemInHand.setItemMeta(meta);
                break;
            case "floats":
                ArrayList<Float> floatList = new ArrayList<>(meta.getCustomModelDataComponent().getFloats());
                try{
                    Float.parseFloat(strings[2]);
                } catch (Exception e){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Please enter a numeric value."));
                    return true;
                }
                if(customModelDataIndex > floatList.size()){
                    for(int i = floatList.size(); i < customModelDataIndex; i++){
                        floatList.add(0f);
                    }
                }
                floatList.set(customModelDataIndex - 1, Float.parseFloat(strings[2]));
                cmdComponent.setFloats(floatList);
                meta.setCustomModelDataComponent(cmdComponent);
                itemInHand.setItemMeta(meta);
                break;
            case "colors":
                ArrayList<Color> colorList = new ArrayList<>(meta.getCustomModelDataComponent().getColors());
                String[] rgb = strings[2].split(",");
                if(rgb.length != 3){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Please enter a color in the format R,G,B (e.g. 255,0,0 for red)."));
                    return true;
                }
                int r, g, b;
                try{
                    r = Integer.parseInt(rgb[0].trim());
                    g = Integer.parseInt(rgb[1].trim());
                    b = Integer.parseInt(rgb[2].trim());
                } catch (NumberFormatException e){
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid value provided. Please enter a color in the format R,G,B (e.g. 255,0,0 for red)."));
                    return true;
                }
                if(customModelDataIndex > colorList.size()){
                    for(int i = colorList.size(); i < customModelDataIndex; i++){
                        colorList.add(Color.fromRGB(0, 0, 0));
                    }
                }
                colorList.set(customModelDataIndex - 1, Color.fromRGB(r, g, b));
                cmdComponent.setColors(colorList);
                meta.setCustomModelDataComponent(cmdComponent);
                itemInHand.setItemMeta(meta);
                break;
            default:
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid custom model data type. Valid types are: strings, floats, flags, colors."));
                return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length == 1){
            return List.of("strings", "flags", "floats", "colors");
        }
        return List.of();
    }
}
