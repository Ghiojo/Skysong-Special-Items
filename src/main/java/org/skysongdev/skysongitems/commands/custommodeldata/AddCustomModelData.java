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

public class AddCustomModelData implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length != 2){
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
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        switch(strings[0].toLowerCase()){
            case "string":
                ArrayList<String> newStrings = new ArrayList<>(meta.getCustomModelDataComponent().getStrings());
                newStrings.add(strings[1]);
                cmd.setStrings(newStrings);
                meta.setCustomModelDataComponent(cmd);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Custom model data with ID <yellow>" + strings[1] + " <green>added successfully!"));
                break;
            case "float":
                ArrayList<Float> newFloats = new ArrayList<>(meta.getCustomModelDataComponent().getFloats());
                try {
                    newFloats.add(Float.parseFloat(strings[1]));
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid float value provided. Please enter a numeric value."));
                    return true;
                }
                cmd.setFloats(newFloats);
                meta.setCustomModelDataComponent(cmd);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Custom model data with ID <yellow>" + strings[1] + " <green>added successfully!"));
                break;
            case "flag":
                ArrayList<Boolean> newFlags = new ArrayList<>(meta.getCustomModelDataComponent().getFlags());
                if(strings[1].equalsIgnoreCase("true") || strings[1].equalsIgnoreCase("false")){
                    newFlags.add(Boolean.parseBoolean(strings[1]));
                } else {
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid flag value provided. Please enter true or false."));
                    return true;
                }
                cmd.setFlags(newFlags);
                meta.setCustomModelDataComponent(cmd);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Custom model data with ID <yellow>" + strings[1] + " <green>added successfully!"));
                break;
            case "color":
                ArrayList<Color> newColors = new ArrayList<>(meta.getCustomModelDataComponent().getColors());
                try {
                    String[] rgb = strings[1].split(",");
                    if (rgb.length != 3) {
                        commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid color format. Please enter in R,G,B format."));
                        return true;
                    }
                    int r = Integer.parseInt(rgb[0].trim());
                    int g = Integer.parseInt(rgb[1].trim());
                    int b = Integer.parseInt(rgb[2].trim());
                    newColors.add(Color.fromRGB(r, g, b));
                } catch (NumberFormatException e) {
                    commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid color value provided. Please enter numeric RGB values in R,G,B format."));
                    return true;
                }
                cmd.setColors(newColors);
                meta.setCustomModelDataComponent(cmd);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Custom model data with ID <yellow>" + strings[1] + " <green>added successfully!"));
                break;
            default:
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid custom model data type. Valid types are: string, float, flag, color."));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length == 1){
            return List.of("string", "float", "flag", "color");
        }
        return List.of();
    }
}
