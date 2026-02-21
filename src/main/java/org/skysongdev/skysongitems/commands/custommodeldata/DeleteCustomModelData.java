package org.skysongdev.skysongitems.commands.custommodeldata;

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

import java.util.List;

import static org.skysongdev.skysongitems.SkysongItems.getPlugin;

public class DeleteCustomModelData implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if(strings.length != 1){
            commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Usage: /addcmd <custom_model_data_id> <value>"));
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
            case "floats":
                CustomModelDataComponent floatsComponent = meta.getCustomModelDataComponent();
                if(floatsComponent != null){
                    floatsComponent.setFloats(List.of());
                }
                meta.setCustomModelDataComponent(floatsComponent);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully deleted all float custom model data from the item!"));
                break;
            case "strings":
                CustomModelDataComponent stringsComponent = meta.getCustomModelDataComponent();
                if(stringsComponent != null){
                    stringsComponent.setStrings(List.of());
                }
                meta.setCustomModelDataComponent(stringsComponent);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully deleted all string custom model data from the item!"));
                break;
            case "flags":
                CustomModelDataComponent flagsComponent = meta.getCustomModelDataComponent();
                if(flagsComponent != null){
                    flagsComponent.setFlags(List.of());
                }
                meta.setCustomModelDataComponent(flagsComponent);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully deleted all flag custom model data from the item!"));
                break;
            case "colors":
                CustomModelDataComponent colorsComponent = meta.getCustomModelDataComponent();
                if(colorsComponent != null){
                    colorsComponent.setColors(List.of());
                }
                meta.setCustomModelDataComponent(colorsComponent);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully deleted all color custom model data from the item!"));
                break;
            case "all":
                meta.setCustomModelDataComponent(null);
                itemInHand.setItemMeta(meta);
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<green>Successfully deleted all custom model data from the item!"));
                break;
            default:
                commandSender.sendMessage(getPlugin().getMiniMessage().deserialize(Utils.pluginTag + "<red>Invalid custom model data type! Valid types are: floats, strings, flags, colors"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        return List.of("floats", "strings", "flags", "colors", "all").stream().filter(type -> type.startsWith(strings[0].toLowerCase())).toList();
    }
}
